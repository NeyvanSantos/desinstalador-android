import { useEffect, useMemo, useState } from 'react';
import {
  ArrowDownAZ,
  ArrowDownToLine,
  ArrowDownWideNarrow,
  ArrowRight,
  Check,
  ChevronLeft,
  Circle,
  Lock,
  Pause,
  Play,
  Search,
  Square,
  Wifi,
  Zap,
} from 'lucide-react';
import {
  APP_FACTS,
  CONCEPTUAL_PHONE_APPS,
  DOWNLOAD_AVAILABLE,
  MockupFilterTab,
  MockupSortOrder,
  OFFICIAL_DOWNLOAD_URL,
  filterAndSortMockupApps,
  getToggleableSelection,
} from '../utils/desinstalaUtils';

const DEMO_SEQUENCE: string[][] = [
  ['c1'],
  ['c1', 'c3'],
  ['c1', 'c3', 'c2'],
];

export default function HeroSection() {
  const [searchQuery, setSearchQuery] = useState('');
  const [filterTab, setFilterTab] = useState<MockupFilterTab>('Instalados');
  const [sortOrder, setSortOrder] = useState<MockupSortOrder>('alpha');
  const [demoIndex, setDemoIndex] = useState(1);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(() => new Set(DEMO_SEQUENCE[1]));
  const [autoPlay, setAutoPlay] = useState(() => {
    if (typeof window === 'undefined') return false;
    return !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  });

  useEffect(() => {
    if (!autoPlay) return;
    const timer = window.setInterval(() => {
      setDemoIndex((prev) => {
        const next = (prev + 1) % DEMO_SEQUENCE.length;
        setSelectedIds(new Set(DEMO_SEQUENCE[next]));
        return next;
      });
    }, 2200);
    return () => window.clearInterval(timer);
  }, [autoPlay]);

  const visibleApps = useMemo(
    () => filterAndSortMockupApps(CONCEPTUAL_PHONE_APPS, searchQuery, filterTab, sortOrder),
    [searchQuery, filterTab, sortOrder]
  );

  const toggleableVisibleApps = useMemo(
    () => visibleApps.filter((app) => !app.protectedSystem),
    [visibleApps]
  );

  const allVisibleSelected =
    toggleableVisibleApps.length > 0 &&
    toggleableVisibleApps.every((app) => selectedIds.has(app.id));

  const selectedApps = useMemo(
    () => CONCEPTUAL_PHONE_APPS.filter((app) => selectedIds.has(app.id)),
    [selectedIds]
  );

  const selectedTotalMb = useMemo(
    () => selectedApps.reduce((acc, app) => acc + app.sizeMb, 0),
    [selectedApps]
  );

  const handleToggleApp = (id: string) => {
    const app = CONCEPTUAL_PHONE_APPS.find((item) => item.id === id);
    if (!app) return;
    setAutoPlay(false);
    setSelectedIds((prev) => getToggleableSelection(prev, app));
  };

  const handleSelectAll = () => {
    setAutoPlay(false);
    if (allVisibleSelected) {
      setSelectedIds((prev) => {
        const next = new Set(prev);
        for (const app of toggleableVisibleApps) {
          next.delete(app.id);
        }
        return next;
      });
      return;
    }

    setSelectedIds((prev) => {
      const next = new Set(prev);
      for (const app of toggleableVisibleApps) {
        next.add(app.id);
      }
      return next;
    });
  };

  const formattedTotalSize =
    selectedTotalMb >= 1
      ? `${selectedTotalMb.toFixed(selectedTotalMb % 1 === 0 ? 0 : 1).replace('.', ',')} MB`
      : '0 MB';

  return (
    <section
      id="topo"
      className="relative overflow-hidden oled-atmosphere border-b border-[#1A2338] pt-10 pb-14 sm:pt-16 sm:pb-20"
    >
      {/* Subtle ambient background glow motion (Halo-inspired, OLED amber/emerald) */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-28 right-[8%] w-[420px] h-[420px] rounded-full bg-[#F59E0B]/10 blur-[120px] ambient-orb"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 oled-grid opacity-30"
      />

      <div className="relative max-w-6xl mx-auto px-4 sm:px-6">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-center">
          {/* Left: Expressive headline with progressive line reveal */}
          <div className="lg:col-span-7 flex flex-col justify-center">
            <p className="text-xs sm:text-sm font-medium text-[#8E9BB0] mb-4 flex flex-wrap items-center gap-x-2.5 gap-y-1">
              <span className="text-[#F59E0B] font-semibold">Versão {APP_FACTS.version}</span>
              <span aria-hidden="true">·</span>
              <span>{APP_FACTS.minAndroid}</span>
              <span aria-hidden="true">·</span>
              <span>Offline e sem root</span>
            </p>

            <h1
              className="text-[#F8FAFC] font-bold tracking-tight leading-[1.06] mb-5 text-balance"
              style={{ fontSize: 'clamp(2.25rem, 4.5vw, 3.75rem)' }}
            >
              <span className="hero-line-reveal hero-line-delay-1">Desinstale vários</span>
              <span className="hero-line-reveal hero-line-delay-2">apps Android</span>
              <span className="hero-line-reveal hero-line-delay-3">em uma fila única.</span>
            </h1>

            <p className="text-base sm:text-lg text-[#8E9BB0] leading-relaxed max-w-2xl mb-8">
              Escolha os aplicativos que não usa mais e inicie a fila. A confirmação por acessibilidade é opcional e só ocorre quando o instalador expõe evidências suficientes; nos demais casos, confirme manualmente. Sem root, sem computador e sem permissão de Internet.
            </p>

            <div className="flex flex-wrap items-center gap-4">
              {DOWNLOAD_AVAILABLE ? (
                <a
                  href={OFFICIAL_DOWNLOAD_URL}
                  className="group btn-lift cta-glow-spin inline-flex items-center justify-center whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
                >
                  <span className="relative z-10 inline-flex items-center justify-center gap-2.5 px-[22px] py-[12px] rounded-[8px] text-base font-semibold text-[#04060A] bg-[#F59E0B] group-hover:bg-[#FBBF24] transition-colors">
                    <ArrowDownToLine className="w-5 h-5" aria-hidden="true" />
                    <span>Baixar APK oficial</span>
                  </span>
                </a>
              ) : (
                <a
                  href="#download"
                  className="inline-flex items-center justify-center whitespace-nowrap shrink-0 rounded-lg border border-[#1A2338] px-[22px] py-[12px] text-base font-semibold text-[#8E9BB0]"
                >
                  Download indisponível
                </a>
              )}

              <a
                href="#como-funciona"
                className="group btn-lift inline-flex items-center justify-center gap-2 px-5 py-3.5 text-sm font-semibold text-[#F1F5F9] hover:text-[#F59E0B] border border-[#1A2338] hover:border-[#F59E0B]/60 bg-[#0A0E17] rounded-lg whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
              >
                <span>Como funciona</span>
                <ArrowRight className="w-4 h-4 arrow-shift" aria-hidden="true" />
              </a>
            </div>
          </div>

          {/* Right: Faithful Phone Mockup matching real Android screen structure */}
          <div className="lg:col-span-5 flex flex-col items-center product-visual-enter">
            <div className="phone-3d-stage relative w-full max-w-[332px] py-3 px-4 flex items-center justify-center">
              <div className="phone-3d-body relative w-full max-w-[298px] aspect-[173/336]">
                {/* Warm OLED rim light & deep cast shadow behind the 3D phone */}
                <div
                  aria-hidden="true"
                  className="pointer-events-none absolute -inset-6 rounded-[48px] phone-3d-rim-glow"
                />
                <div
                  aria-hidden="true"
                  className="pointer-events-none absolute -inset-3 rounded-[44px] phone-3d-shadow"
                />

                {/* 3D metallic chassis depth edge */}
                <div
                  aria-hidden="true"
                  className="pointer-events-none absolute inset-[2px] rounded-[42px] phone-3d-depth-layer"
                />

                {/* Rotating perimeter glow orbiting the 3D phone */}
                <div aria-hidden="true" className="phone-glow-ring" />

                {/* Interactive DesinstalaJá Screen aligned inside Celular_png_b1216ee2.png aperture */}
                <div
                  className="absolute z-10 bg-[#0B0E14] rounded-[34px] overflow-hidden flex flex-col"
                  style={{
                    left: '3.4%',
                    right: '4.0%',
                    top: '1.7%',
                    bottom: '1.7%',
                  }}
                >
                  {/* Diagonal glass reflection */}
                  <div
                    aria-hidden="true"
                    className="pointer-events-none absolute inset-0 z-30 phone-3d-glass-sheen"
                  />

                  {/* Android Status Bar flanking the top notch */}
                  <div className="px-4 pt-2 pb-1.5 flex items-center justify-between text-[10px] font-medium text-[#F1F5F9] tabular-nums relative z-10">
                    <span>12:24</span>
                    <div className="flex items-center gap-1.5 text-[#F1F5F9]">
                      <span className="text-[9px] font-semibold text-[#8E9BB0]">5G</span>
                      <Wifi className="w-2.5 h-2.5 text-[#F1F5F9]" aria-hidden="true" />
                      <span className="px-1 py-0.5 rounded bg-[#F59E0B] text-[#04060A] text-[8px] font-bold leading-none">
                        49
                      </span>
                    </div>
                  </div>

                  {/* App Header (Title updated from "Desinstalador" to "DesinstalaJá") */}
                  <div className="px-3 pt-1.5 pb-2 relative z-10">
                    <div className="flex items-center justify-between gap-2">
                      <p
                        className="text-base font-bold tracking-tight text-[#F8FAFC]"
                        style={{ fontFamily: 'var(--font-display)' }}
                      >
                        DesinstalaJá
                      </p>
                      <button
                        type="button"
                        onClick={() => setAutoPlay((prev) => !prev)}
                        aria-label={autoPlay ? 'Pausar animação da fila' : 'Animar seleção em fila'}
                        className="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-medium text-[#8E9BB0] hover:text-[#F8FAFC] border border-[#1A2338] rounded-md bg-[#0F1523] cursor-pointer whitespace-nowrap"
                      >
                        {autoPlay ? (
                          <Pause className="w-2.5 h-2.5" aria-hidden="true" />
                        ) : (
                          <Play className="w-2.5 h-2.5" aria-hidden="true" />
                        )}
                        <span>{autoPlay ? 'Pausar' : 'Animar'}</span>
                      </button>
                    </div>
                    <p className="text-[9.5px] text-[#8E9BB0] leading-snug mt-0.5 whitespace-nowrap truncate">
                      Selecione múltiplos apps para remoção em lote
                    </p>
                  </div>

                  {/* Main Content Area */}
                  <div className="px-3 space-y-2 pb-2 flex-1 flex flex-col justify-between relative z-10">
                    <div className="space-y-2">
                      {/* Accessibility Notice Card */}
                      <div className="rounded-xl border border-[#1A2338] bg-[#111622] px-2.5 py-2 flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-[#1B2234] border border-[#26314A] flex items-center justify-center shrink-0">
                          <Zap className="w-3 h-3 text-[#F1F5F9]" aria-hidden="true" />
                        </div>
                        <div className="min-w-0">
                          <p className="text-[11px] font-semibold text-[#F8FAFC] leading-tight">
                            Desinstalação automática desligada
                          </p>
                          <p className="text-[9.5px] text-[#8E9BB0] leading-snug mt-0.5">
                            Toque aqui para ativar o serviço nas configurações do celular.
                          </p>
                        </div>
                      </div>

                      {/* Search Input */}
                      <label className="relative flex items-center rounded-xl border border-[#1A2338] bg-[#111622] px-2.5 py-1.5">
                        <Search className="w-3 h-3 text-[#F59E0B] shrink-0 mr-1.5" aria-hidden="true" />
                        <input
                          type="text"
                          value={searchQuery}
                          onChange={(event) => {
                            setAutoPlay(false);
                            setSearchQuery(event.target.value);
                          }}
                          placeholder="Buscar por nome ou pacote..."
                          aria-label="Buscar aplicativo na lista de exemplo"
                          className="w-full bg-transparent text-[10.5px] text-[#F8FAFC] placeholder:text-[#8E9BB0] focus:outline-none"
                        />
                      </label>

                      {/* Filter & Sort Controls Row */}
                      <div
                        className="flex items-center justify-between gap-1 pb-0.5"
                        role="group"
                        aria-label="Filtros e ordenação da lista de apps"
                      >
                        <button
                          type="button"
                          onClick={() => {
                            setAutoPlay(false);
                            setFilterTab('Instalados');
                          }}
                          aria-pressed={filterTab === 'Instalados'}
                          className={`px-2 py-1 rounded-md text-[9.5px] font-semibold whitespace-nowrap shrink-0 cursor-pointer transition-colors ${
                            filterTab === 'Instalados'
                              ? 'bg-[#F59E0B] text-[#04060A]'
                              : 'bg-[#111622] text-[#8E9BB0] border border-[#1A2338] hover:text-[#F8FAFC]'
                          }`}
                        >
                          Instalados
                        </button>

                        <button
                          type="button"
                          onClick={() => {
                            setAutoPlay(false);
                            setFilterTab('Sistema');
                          }}
                          aria-pressed={filterTab === 'Sistema'}
                          className={`px-2 py-1 rounded-md text-[9.5px] font-semibold whitespace-nowrap shrink-0 cursor-pointer transition-colors ${
                            filterTab === 'Sistema'
                              ? 'bg-[#F59E0B] text-[#04060A]'
                              : 'bg-[#111622] text-[#8E9BB0] border border-[#1A2338] hover:text-[#F8FAFC]'
                          }`}
                        >
                          Sistema
                        </button>

                        <button
                          type="button"
                          onClick={() => {
                            setAutoPlay(false);
                            setSortOrder('alpha');
                          }}
                          aria-pressed={sortOrder === 'alpha'}
                          className={`inline-flex items-center gap-0.5 px-1.5 py-1 rounded-md text-[9.5px] font-semibold whitespace-nowrap shrink-0 cursor-pointer transition-colors ${
                            sortOrder === 'alpha'
                              ? 'bg-[#F59E0B] text-[#04060A]'
                              : 'bg-[#111622] text-[#8E9BB0] border border-[#1A2338] hover:text-[#F8FAFC]'
                          }`}
                        >
                          <ArrowDownAZ className="w-2.5 h-2.5" aria-hidden="true" />
                          <span>A → Z</span>
                        </button>

                        <button
                          type="button"
                          onClick={() => {
                            setAutoPlay(false);
                            setSortOrder('size');
                          }}
                          aria-pressed={sortOrder === 'size'}
                          className={`inline-flex items-center gap-0.5 px-1.5 py-1 rounded-md text-[9.5px] font-semibold whitespace-nowrap shrink-0 cursor-pointer transition-colors ${
                            sortOrder === 'size'
                              ? 'bg-[#F59E0B] text-[#04060A]'
                              : 'bg-[#111622] text-[#8E9BB0] border border-[#1A2338] hover:text-[#F8FAFC]'
                          }`}
                        >
                          <ArrowDownWideNarrow className="w-2.5 h-2.5" aria-hidden="true" />
                          <span>Maior tam.</span>
                        </button>
                      </div>

                      {/* Count + Select All Row */}
                      <div className="flex items-center justify-between gap-2 text-[10px]">
                        <span className="text-[#8E9BB0] tabular-nums">
                          {visibleApps.length}{' '}
                          {visibleApps.length === 1 ? 'aplicativo de exemplo' : 'aplicativos de exemplo'}
                        </span>
                        <button
                          type="button"
                          onClick={handleSelectAll}
                          disabled={toggleableVisibleApps.length === 0}
                          className="font-semibold text-[#F59E0B] hover:text-[#FBBF24] disabled:opacity-40 cursor-pointer whitespace-nowrap"
                        >
                          {allVisibleSelected ? 'Limpar seleção' : 'Selecionar todos'}
                        </button>
                      </div>

                      {/* App List Rows matching screenshot structure */}
                      <div className="space-y-1.5">
                        {visibleApps.length === 0 ? (
                          <div className="rounded-xl border border-[#1A2338] bg-[#111622] px-3 py-5 text-center text-[10px] text-[#8E9BB0]">
                            Nenhum aplicativo de exemplo corresponde ao filtro.
                          </div>
                        ) : (
                          visibleApps.map((app) => {
                            const checked = selectedIds.has(app.id);
                            const locked = Boolean(app.protectedSystem);
                            return (
                              <button
                                key={app.id}
                                type="button"
                                disabled={locked}
                                onClick={() => handleToggleApp(app.id)}
                                aria-pressed={checked}
                                aria-label={
                                  locked
                                    ? `${app.name} protegido contra seleção`
                                    : `Selecionar ${app.name}`
                                }
                                className={`w-full text-left px-2.5 py-1.5 rounded-xl border transition-colors flex items-center gap-2 ${
                                  locked
                                    ? 'border-[#1A2338] bg-[#111622]/60 opacity-65 cursor-not-allowed'
                                    : checked
                                      ? 'border-[#F59E0B]/70 bg-[#F59E0B]/10 cursor-pointer'
                                      : 'border-[#1A2338] bg-[#111622] hover:border-[#8E9BB0]/40 cursor-pointer'
                                }`}
                              >
                                {/* Left Checkbox */}
                                <span
                                  aria-hidden="true"
                                  className={`w-3.5 h-3.5 rounded flex items-center justify-center border shrink-0 transition-colors ${
                                    locked
                                      ? 'border-[#2D384F] bg-[#0B0E14] text-[#8E9BB0]'
                                      : checked
                                        ? 'border-[#F59E0B] bg-[#F59E0B] text-[#04060A]'
                                        : 'border-[#475569] bg-transparent'
                                  }`}
                                >
                                  {locked ? (
                                    <Lock className="w-2 h-2" />
                                  ) : checked ? (
                                    <Check className="w-2.5 h-2.5 stroke-[3]" />
                                  ) : null}
                                </span>

                                {/* Colored Generic App Icon Tile */}
                                <span
                                  aria-hidden="true"
                                  className="w-8 h-8 rounded-lg flex items-center justify-center text-[10px] font-bold tracking-tight shrink-0"
                                  style={{ backgroundColor: app.iconBg, color: app.iconFg }}
                                >
                                  {app.iconCode}
                                </span>

                                {/* App Name + Amber Size + Package ID */}
                                <div className="min-w-0 flex-1">
                                  <p className="text-[11px] font-semibold text-[#F8FAFC] truncate leading-tight">
                                    {app.name}
                                  </p>
                                  <p className="text-[9.5px] truncate mt-0.5 tabular-nums">
                                    <span className="font-semibold text-[#F59E0B]">{app.sizeLabel}</span>
                                    <span className="text-[#8E9BB0] mx-1">·</span>
                                    <span className="font-mono text-[#8E9BB0]">{app.packageId}</span>
                                  </p>
                                </div>
                              </button>
                            );
                          })
                        )}
                      </div>
                    </div>

                    {/* Queue Formation Summary Bar */}
                    <div
                      className={`rounded-xl border px-2.5 py-1.5 flex items-center justify-between gap-2 transition-colors ${
                        selectedApps.length > 0
                          ? 'border-[#F59E0B]/60 bg-[#0F1523]'
                          : 'border-[#1A2338] bg-[#0A0E17]'
                      }`}
                      data-demo-step={demoIndex}
                    >
                      <div className="min-w-0">
                        <p className="text-[10px] font-semibold text-[#F8FAFC] tabular-nums truncate">
                          {selectedApps.length > 0
                            ? `Fila pronta: ${selectedApps.length} ${
                                selectedApps.length === 1 ? 'app' : 'apps'
                              } (${formattedTotalSize})`
                            : 'Marque apps para formar a fila'}
                        </p>
                        <p className="text-[9px] text-[#8E9BB0] truncate">
                          {selectedApps.length > 0
                            ? selectedApps.map((item) => item.name.split(' ')[0]).join(' → ')
                            : 'Diálogos nativos em sequência'}
                        </p>
                      </div>
                      <span
                        className={`px-2 py-1 rounded-md text-[9.5px] font-semibold whitespace-nowrap shrink-0 ${
                          selectedApps.length > 0
                            ? 'bg-[#F59E0B] text-[#04060A]'
                            : 'bg-[#1A2338] text-[#8E9BB0]'
                        }`}
                      >
                        Iniciar fila
                      </span>
                    </div>
                  </div>

                  {/* Android 3-button Bottom Navigation */}
                  <div
                    aria-hidden="true"
                    className="px-9 py-1.5 border-t border-[#1A2338] bg-[#07090E] flex items-center justify-between text-[#8E9BB0] relative z-10"
                  >
                    <Square className="w-2.5 h-2.5" />
                    <Circle className="w-2.5 h-2.5" />
                    <ChevronLeft className="w-3 h-3" />
                  </div>
                </div>

                {/* Provided Smartphone Hardware Frame (Celular_png_b1216ee2.png) */}
                <div
                  aria-hidden="true"
                  className="pointer-events-none absolute inset-0 z-20 overflow-visible"
                >
                  <img
                    src="/assets/aistudio/Celular_png_b1216ee2.png"
                    alt=""
                    className="absolute max-w-none select-none drop-shadow-[0_18px_28px_rgba(0,0,0,0.75)]"
                    style={{
                      width: '208.09%',
                      height: '107.14%',
                      left: '-57.23%',
                      top: '-3.27%',
                    }}
                  />
                </div>
              </div>
            </div>

            <p className="text-xs text-[#8E9BB0] text-center mt-3 max-w-[340px]">
              Reprodução da interface do app com aplicativos genéricos de exemplo.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
