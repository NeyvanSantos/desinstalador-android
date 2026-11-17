import QRCode from 'qrcode';

export const OFFICIAL_SITE_URL = 'https://desinstalador-android.vercel.app';
export const OFFICIAL_DOWNLOAD_URL =
  'https://drive.usercontent.google.com/download?id=1A3_Tfgt3e6aMz2w-SveTy4DrbQICUGoy&export=download&confirm=t';

export const APP_FACTS = {
  name: 'DesinstalaJá',
  version: '1.1',
  sizeLabel: '~1,13 MB',
  minAndroid: 'Android 8.0+',
  internetPermission: false,
  rootRequired: false,
} as const;

export type MockupFilterTab = 'Instalados' | 'Sistema';
export type MockupSortOrder = 'alpha' | 'size';

export interface ConceptualAppItem {
  id: string;
  name: string;
  packageId: string;
  sizeMb: number;
  sizeLabel: string;
  iconCode: string;
  iconBg: string;
  iconFg: string;
  isSystem: boolean;
  protectedSystem?: boolean;
}

export const CONCEPTUAL_PHONE_APPS: ConceptualAppItem[] = [
  {
    id: 'c1',
    name: 'Bloco de Notas Rápido',
    packageId: 'com.exemplo.notas',
    sizeMb: 28,
    sizeLabel: '28 MB',
    iconCode: 'BN',
    iconBg: '#FACC15',
    iconFg: '#04060A',
    isSystem: false,
  },
  {
    id: 'c2',
    name: 'Editor de Fotos Compacto',
    packageId: 'com.exemplo.fotolite',
    sizeMb: 142,
    sizeLabel: '142 MB',
    iconCode: 'EF',
    iconBg: '#0EA5E9',
    iconFg: '#FFFFFF',
    isSystem: false,
  },
  {
    id: 'c3',
    name: 'Jogo Corrida Arcade',
    packageId: 'com.exemplo.arcade',
    sizeMb: 284,
    sizeLabel: '284 MB',
    iconCode: 'JA',
    iconBg: '#16A34A',
    iconFg: '#FFFFFF',
    isSystem: false,
  },
  {
    id: 'c4',
    name: 'Leitor de Documentos',
    packageId: 'com.exemplo.leitorpdf',
    sizeMb: 67.4,
    sizeLabel: '67,4 MB',
    iconCode: 'LD',
    iconBg: '#1D4ED8',
    iconFg: '#FFFFFF',
    isSystem: false,
  },
  {
    id: 'c5',
    name: 'Serviços do Sistema',
    packageId: 'com.android.systemui',
    sizeMb: 48.2,
    sizeLabel: '48,2 MB',
    iconCode: 'SYS',
    iconBg: '#334155',
    iconFg: '#F8FAFC',
    isSystem: true,
    protectedSystem: true,
  },
];

export function filterAndSortMockupApps(
  apps: ConceptualAppItem[],
  query: string,
  tab: MockupFilterTab,
  sortOrder: MockupSortOrder
): ConceptualAppItem[] {
  const normalizedQuery = query.trim().toLowerCase();

  const filtered = apps.filter((app) => {
    const matchesTab = tab === 'Sistema' ? app.isSystem : !app.isSystem;
    if (!matchesTab) return false;
    if (!normalizedQuery) return true;
    return (
      app.name.toLowerCase().includes(normalizedQuery) ||
      app.packageId.toLowerCase().includes(normalizedQuery)
    );
  });

  return [...filtered].sort((a, b) => {
    if (sortOrder === 'size') {
      return b.sizeMb - a.sizeMb;
    }
    return a.name.localeCompare(b.name, 'pt-BR');
  });
}

export interface WorkflowStep {
  id: 1 | 2 | 3;
  indexLabel: string;
  title: string;
  shortLabel: string;
  description: string;
  detailNote: string;
}

export const WORKFLOW_STEPS: WorkflowStep[] = [
  {
    id: 1,
    indexLabel: '01',
    title: 'Selecione vários aplicativos da lista',
    shortLabel: 'Selecionar apps',
    description:
      'Marque quantos aplicativos desejar na lista instalada. Pacotes críticos do sistema contam com proteção contra seleção acidental.',
    detailNote: 'Seleção múltipla local sem envio de lista para servidores.',
  },
  {
    id: 2,
    indexLabel: '02',
    title: 'Inicie a fila de desinstalações',
    shortLabel: 'Iniciar fila',
    description:
      'O DesinstalaJá enfileira os pedidos e aciona os diálogos oficiais do instalador do Android em sequência, sem você ter que voltar à lista a cada item.',
    detailNote:
      'Estimativa aproximada: cerca de 12 segundos para 15 apps quando a acessibilidade está ativa — o tempo real varia conforme o aparelho e a resposta dos diálogos do Android.',
  },
  {
    id: 3,
    indexLabel: '03',
    title: 'Acessibilidade confirma os diálogos nativos',
    shortLabel: 'Confirmar diálogos',
    description:
      'Com o serviço de acessibilidade habilitado explicitamente por você, o app toca no botão de confirmação dos diálogos nativos do Android respeitando limites de segurança.',
    detailNote:
      'Não é desinstalação silenciosa: o fluxo usa as telas oficiais do sistema e pode ser interrompido a qualquer momento.',
  },
];

export interface PermissionItem {
  permission: string;
  required: string;
  purpose: string;
}

export const MANIFEST_PERMISSIONS: PermissionItem[] = [
  {
    permission: 'QUERY_ALL_PACKAGES',
    required: 'Obrigatória',
    purpose: 'Permite listar os aplicativos instalados no aparelho para que você selecione o que deseja remover.',
  },
  {
    permission: 'REQUEST_DELETE_PACKAGES',
    required: 'Obrigatória',
    purpose: 'Solicita ao instalador oficial do Android a abertura do diálogo nativo de desinstalação para cada item da fila.',
  },
  {
    permission: 'BIND_ACCESSIBILITY_SERVICE',
    required: 'Ativação manual pelo usuário',
    purpose: 'Opcional para confirmação assistida: após habilitação explícita nos ajustes do Android, reconhece e confirma o botão do diálogo nativo durante a fila.',
  },
];

export const PROTECTED_CRITICAL_PACKAGES = new Set<string>([
  'com.android.systemui',
  'com.android.settings',
  'com.android.packageinstaller',
  'com.google.android.packageinstaller',
  'com.android.phone',
]);

export function isCriticalPackageProtected(packageId: string): boolean {
  const normalized = packageId.trim().toLowerCase();
  return PROTECTED_CRITICAL_PACKAGES.has(normalized) || normalized.startsWith('android.');
}

export function getToggleableSelection(
  currentSelectedIds: Set<string>,
  app: ConceptualAppItem
): Set<string> {
  if (app.protectedSystem || isCriticalPackageProtected(app.packageId)) {
    return new Set(currentSelectedIds);
  }
  const next = new Set(currentSelectedIds);
  if (next.has(app.id)) {
    next.delete(app.id);
  } else {
    next.add(app.id);
  }
  return next;
}

export interface QrMatrix {
  size: number;
  modules: boolean[][];
}

export function generateQrMatrix(text: string): QrMatrix {
  const qr = QRCode.create(text, { errorCorrectionLevel: 'M' });
  const size = qr.modules.size;
  const data = qr.modules.data;
  const modules: boolean[][] = [];

  for (let row = 0; row < size; row += 1) {
    const line: boolean[] = [];
    for (let col = 0; col < size; col += 1) {
      line.push(Boolean(data[row * size + col]));
    }
    modules.push(line);
  }

  return { size, modules };
}
