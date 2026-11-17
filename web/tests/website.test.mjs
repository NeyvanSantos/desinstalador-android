import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';

const htmlPath = path.resolve('g:/Desinstalador de Android/web/index.html');
const html = fs.readFileSync(htmlPath, 'utf8');

test('1. [Semântica HTML5] Estrutura hierárquica e tags semânticas completas', () => {
  assert.ok(html.includes('<header'), 'Header deve estar presente');
  assert.ok(html.includes('<nav'), 'Nav deve estar presente');
  assert.ok(html.includes('<main'), 'Main deve estar presente');
  assert.ok(html.includes('<section'), 'Section deve estar presente');
  assert.ok(html.includes('<footer'), 'Footer deve estar presente');
  
  const h1Matches = html.match(/<h1[\s>]/g) || [];
  assert.strictEqual(h1Matches.length, 1, 'Deve existir exatamente 1 elemento <h1> na página');
});

test('2. [Link de Download] Link direto com bypass do Google Drive confirm=t', () => {
  const directLinkRegex = /https:\/\/drive\.usercontent\.google\.com\/download\?id=1A3_Tfgt3e6aMz2w-SveTy4DrbQICUGoy&export=download&confirm=t/;
  assert.ok(directLinkRegex.test(html), 'Link de download direto do APK com confirm=t deve estar configurado');
});

test('3. [ECC - Tipografia & Tabular Nums] text-wrap e tabular-nums aplicados', () => {
  assert.ok(html.includes('text-wrap: balance'), 'CSS deve conter text-wrap: balance para títulos');
  assert.ok(html.includes('text-wrap: pretty'), 'CSS deve conter text-wrap: pretty para parágrafos');
  assert.ok(html.includes('tabular-nums'), 'Classes tabular-nums devem estar presentes nas métricas numéricas');
});

test('4. [ECC - Feedback Tátil] Microinteração active:scale nos botões principais', () => {
  assert.ok(html.includes('active:scale-[0.97]'), 'Botões devem possuir resposta tátil com active:scale-[0.97]');
});

test('5. [Acessibilidade WCAG 2.2 AA] Diálogo Modal de QR Code com suporte a teclado ESC', () => {
  assert.ok(html.includes('role="dialog"'), 'Modal deve possuir role="dialog"');
  assert.ok(html.includes('aria-modal="true"'), 'Modal deve possuir aria-modal="true"');
  assert.ok(html.includes('aria-labelledby="qr-modal-title"'), 'Modal deve possuir aria-labelledby');
  assert.ok(html.includes("'Escape'"), 'JavaScript deve escutar a tecla Escape para fechar o diálogo');
});

test('6. [SEO & Metadados] Schema.org SoftwareApplication e Open Graph', () => {
  assert.ok(html.includes('"@type": "SoftwareApplication"'), 'JSON-LD deve declarar SoftwareApplication');
  assert.ok(html.includes('property="og:title"'), 'Open Graph og:title deve estar presente');
  assert.ok(html.includes('property="og:image"'), 'Open Graph og:image deve estar presente');
  assert.ok(html.includes('rel="canonical"'), 'Tag canonical deve estar presente');
  assert.ok(html.includes('name="theme-color"'), 'Tag theme-color deve estar presente');
});

test('7. [Design Distinto - Diagnóstico por Fabricante OEM] Filtros Xiaomi, Samsung e Pixel', () => {
  assert.ok(html.includes('data-oem="xiaomi"') || html.includes('selectOem("xiaomi")') || html.includes('id="oem-selector"'), 'Deve conter seletor de diagnósticos por fabricante Android (Xiaomi, Samsung, Pixel)');
});

test('8. [Design Distinto - Storage Breakdown Gauge] Régua visual de armazenamento em disco', () => {
  assert.ok(html.includes('id="storage-gauge"') || html.includes('storage-breakdown'), 'Deve conter o gráfico/régua de armazenamento em disco dinâmico');
});

test('9. [Design Distinto - Inspetor de Engenharia & Código Real] Trechos Kotlin/Compose', () => {
  assert.ok(html.includes('BatchUninstallService') || html.includes('AccessibilityService'), 'Deve exibir a engenharia real com trecho de código auditável');
});
