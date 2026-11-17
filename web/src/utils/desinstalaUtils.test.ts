import test from 'node:test';
import assert from 'node:assert/strict';
import {
  APP_FACTS,
  CONCEPTUAL_PHONE_APPS,
  DOWNLOAD_AVAILABLE,
  OFFICIAL_DOWNLOAD_URL,
  OFFICIAL_SITE_URL,
  WORKFLOW_STEPS,
  filterAndSortMockupApps,
  generateQrMatrix,
  getToggleableSelection,
  isCriticalPackageProtected,
} from './desinstalaUtils';

test('confirmed product facts and official URLs match specifications', () => {
  assert.equal(APP_FACTS.name, 'DesinstalaJá');
  assert.equal(APP_FACTS.version, '1.2');
  assert.equal(APP_FACTS.sizeLabel, '1.96 MB');
  assert.equal(APP_FACTS.minAndroid, 'Android 8.0+');
  assert.equal(APP_FACTS.internetPermission, false);
  assert.equal(APP_FACTS.rootRequired, false);
  assert.equal(DOWNLOAD_AVAILABLE, true);

  assert.equal(OFFICIAL_DOWNLOAD_URL, '/downloads/desinstalaja-v1.2.apk');
  assert.equal(OFFICIAL_SITE_URL, 'https://desinstalador-android.vercel.app');
  assert.equal(WORKFLOW_STEPS.length, 3);
});

test('workflow explains manual fallback and avoids unmeasured speed claims', () => {
  assert.doesNotMatch(WORKFLOW_STEPS[1].detailNote, /12 segundos/);
  assert.match(WORKFLOW_STEPS[1].detailNote, /não há meta de tempo medida/);
  assert.match(WORKFLOW_STEPS[1].description, /variam conforme o instalador/);
  assert.match(WORKFLOW_STEPS[2].description, /aplicativo de destino/);
  assert.match(WORKFLOW_STEPS[2].detailNote, /confirmação manual/);
  assert.match(WORKFLOW_STEPS[1].detailNote, /verificar a remoção/);
});

test('critical packages cannot be toggled into uninstall selection', () => {
  assert.equal(isCriticalPackageProtected('com.android.systemui'), true);
  assert.equal(isCriticalPackageProtected('com.android.packageinstaller'), true);
  assert.equal(isCriticalPackageProtected('com.exemplo.arcade'), false);

  const systemApp = CONCEPTUAL_PHONE_APPS.find((a) => a.protectedSystem);
  const regularApp = CONCEPTUAL_PHONE_APPS.find((a) => !a.protectedSystem);
  assert.ok(systemApp);
  assert.ok(regularApp);

  const afterSystem = getToggleableSelection(new Set(), systemApp);
  assert.equal(afterSystem.size, 0);

  const afterRegular = getToggleableSelection(new Set(), regularApp);
  assert.equal(afterRegular.size, 1);
  assert.equal(afterRegular.has(regularApp.id), true);
});

test('filterAndSortMockupApps filters Instalados/Sistema, searches, and sorts by A-Z or size', () => {
  const installedAlpha = filterAndSortMockupApps(CONCEPTUAL_PHONE_APPS, '', 'Instalados', 'alpha');
  assert.equal(installedAlpha.length, 4);
  assert.ok(installedAlpha.every((a) => !a.isSystem));

  const installedBySize = filterAndSortMockupApps(CONCEPTUAL_PHONE_APPS, '', 'Instalados', 'size');
  assert.ok(installedBySize[0].sizeMb >= installedBySize[1].sizeMb);

  const systemOnly = filterAndSortMockupApps(CONCEPTUAL_PHONE_APPS, '', 'Sistema', 'alpha');
  assert.equal(systemOnly.length, 1);
  assert.equal(systemOnly[0].packageId, 'com.android.systemui');

  const searched = filterAndSortMockupApps(CONCEPTUAL_PHONE_APPS, 'notas', 'Instalados', 'alpha');
  assert.equal(searched.length, 1);
  assert.equal(searched[0].packageId, 'com.exemplo.notas');
});

test('generateQrMatrix produces a valid square QR matrix for the official domain', () => {
  const qr = generateQrMatrix(OFFICIAL_SITE_URL);
  assert.ok(qr.size >= 21);
  assert.equal(qr.modules.length, qr.size);
  assert.equal(qr.modules[0].length, qr.size);
  assert.equal(qr.modules[0][0], true);
  assert.equal(qr.modules[0][6], true);
  assert.equal(qr.modules[6][0], true);
});
