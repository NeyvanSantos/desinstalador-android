import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const webRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const readText = (relativePath) =>
  fs.readFileSync(path.join(webRoot, relativePath), 'utf8');

const html = readText('index.html');
const app = readText('src/App.tsx');
const hero = readText('src/components/HeroSection.tsx');
const howItWorks = readText('src/components/HowItWorksSteps.tsx');
const download = readText('src/components/DownloadSection.tsx');
const faq = readText('src/components/FaqSection.tsx');
const footer = readText('src/components/ClosingCtaAndFooter.tsx');
const utils = readText('src/utils/desinstalaUtils.ts');

test('the React page keeps one semantic heading and its main page landmarks', () => {
  assert.match(readText('src/components/Navbar.tsx'), /<header/);
  assert.match(readText('src/components/Navbar.tsx'), /<nav/);
  assert.match(app, /<main/);
  assert.match(hero, /<section/);
  assert.match(footer, /<footer/);

  const h1Matches = hero.match(/<h1(?:\s|>)/g) || [];
  assert.equal(h1Matches.length, 1);
});

test('SEO metadata, branded assets, and the Vercel output are configured', () => {
  assert.match(html, /<title>DesinstalaJá/);
  assert.match(html, /"@type": "SoftwareApplication"/);
  assert.match(html, /property="og:title"/);
  assert.match(html, /property="og:image"/);
  assert.match(html, /name="theme-color"/);
  assert.match(html, /rel="canonical"/);

  const iconPath = path.join(webRoot, 'public', 'favicon.ico');
  const icon = fs.readFileSync(iconPath);
  assert.deepEqual([...icon.subarray(0, 4)], [0, 0, 1, 0]);
  assert.ok(icon.readUInt16LE(4) > 0);
  assert.ok(fs.existsSync(path.join(webRoot, 'public', 'logo.png')));
  assert.equal(JSON.parse(readText('vercel.json')).outputDirectory, 'dist');
});

test('all download actions use the current official APK URL', () => {
  const officialUrl =
    'https://drive.usercontent.google.com/download?id=1A3_Tfgt3e6aMz2w-SveTy4DrbQICUGoy&export=download&confirm=t';

  assert.ok(utils.includes(officialUrl));
  assert.match(hero, /href=\{OFFICIAL_DOWNLOAD_URL\}/);
  assert.match(readText('src/components/Navbar.tsx'), /href=\{OFFICIAL_DOWNLOAD_URL\}/);
  assert.match(download, /href=\{OFFICIAL_DOWNLOAD_URL\}/);
  assert.match(footer, /href=\{OFFICIAL_DOWNLOAD_URL\}/);
});

test('the download section does not link back to the current site', () => {
  assert.doesNotMatch(download, /href=\{OFFICIAL_SITE_URL\}/);
  assert.match(download, /generateQrMatrix\(OFFICIAL_SITE_URL\)/);
});

test('the primary download action fills the available card width', () => {
  const downloadLink = download.match(
    /<a\s+href=\{OFFICIAL_DOWNLOAD_URL\}[\s\S]*?<\/a>/
  )?.[0];

  assert.ok(downloadLink);
  assert.match(downloadLink, /className="[^"]*\bw-full\b/);
  assert.match(downloadLink, /<span className="[^"]*\bw-full\b/);
});

test('the QR card is hidden below desktop width', () => {
  const asideClass = download.match(/<aside className="([^"]+)"/)?.[1];
  assert.ok(asideClass);
  assert.ok(asideClass.split(/\s+/).includes('hidden'));
  assert.ok(asideClass.split(/\s+/).includes('lg:flex'));
});

test('the demo mockup is clearly labeled and keeps the sample selection interactive', () => {
  assert.match(hero, /aplicativos de exemplo/);
  assert.match(hero, /aria-label="Buscar aplicativo na lista de exemplo"/);
  assert.match(hero, /filterAndSortMockupApps/);
  assert.match(hero, /handleSelectAll/);
  assert.match(utils, /name: 'DesinstalaJá'/);
});

test('interactive controls and legal dialog expose accessible keyboard states', () => {
  assert.match(howItWorks, /role="group"/);
  assert.match(howItWorks, /aria-pressed=\{selected\}/);
  assert.match(faq, /aria-controls=/);
  assert.match(faq, /inert=\{!isOpen\}/);
  assert.match(footer, /role="dialog"/);
  assert.match(footer, /aria-modal="true"/);
  assert.match(footer, /aria-describedby=/);
  assert.match(footer, /event\.key (?:===|!==) 'Tab'/);
  assert.match(footer, /restoreFocusRef\.current\?\.focus\(\)/);
  assert.match(readText('src/index.css'), /prefers-reduced-motion: reduce/);
});
