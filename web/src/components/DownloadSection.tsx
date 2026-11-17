import { useMemo, useState } from 'react';
import { ArrowDownToLine, Check, Copy } from 'lucide-react';
import {
  APP_FACTS,
  DOWNLOAD_AVAILABLE,
  OFFICIAL_DOWNLOAD_URL,
  OFFICIAL_SITE_URL,
  generateQrMatrix,
} from '../utils/desinstalaUtils';
import { useScrollReveal } from '../utils/useScrollReveal';

export default function DownloadSection() {
  const [copyState, setCopyState] = useState<'idle' | 'copied' | 'error'>('idle');
  const { ref, visible } = useScrollReveal();

  const qr = useMemo(() => generateQrMatrix(OFFICIAL_SITE_URL), []);

  const handleCopySiteUrl = async () => {
    try {
      await navigator.clipboard.writeText(OFFICIAL_SITE_URL);
      setCopyState('copied');
    } catch (error) {
      console.error('Não foi possível copiar o endereço oficial do site.', error);
      setCopyState('error');
    }
    window.setTimeout(() => setCopyState('idle'), 2000);
  };

  return (
    <section
      id="download"
      className="overflow-x-hidden py-16 sm:py-24 bg-[#04060A] border-b border-[#1A2338]"
    >
      <div
        ref={ref}
        className={`max-w-6xl mx-auto px-4 sm:px-6 scroll-reveal-alt ${visible ? 'is-visible' : ''}`}
      >
        <div className="max-w-2xl mb-10 alt-enter-head">
          <p className="text-sm font-medium text-[#F59E0B] mb-2">Distribuição oficial</p>
          <h2 className="text-2xl sm:text-4xl font-bold tracking-tight text-[#F8FAFC] text-balance">
            Baixe o APK oficial do DesinstalaJá.
          </h2>
          <p className="text-sm sm:text-base text-[#8E9BB0] leading-relaxed mt-3">
            Versão 1.2 assinada e verificada, hospedada diretamente neste domínio.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
          {/* Left: Official APK Specs & Direct Download */}
          <div className="lg:col-span-7 bg-[#0A0E17] border border-[#1A2338] rounded-xl p-6 sm:p-8 flex flex-col justify-between gap-6 alt-enter-left">
            <div>
              <dl className="grid grid-cols-2 sm:grid-cols-4 gap-5 pb-6 border-b border-[#1A2338]">
                <div>
                  <dt className="text-xs text-[#8E9BB0]">Versão atual</dt>
                  <dd className="text-xl font-bold text-[#F8FAFC] font-mono tabular-nums mt-1">
                    {APP_FACTS.version}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs text-[#8E9BB0]">Tamanho</dt>
                  <dd className="text-xl font-bold text-[#F8FAFC] font-mono tabular-nums mt-1">
                    {APP_FACTS.sizeLabel}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs text-[#8E9BB0]">Requisito</dt>
                  <dd className="text-lg sm:text-xl font-bold text-[#F8FAFC] whitespace-nowrap mt-1">
                    {APP_FACTS.minAndroid}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs text-[#8E9BB0]">Conectividade</dt>
                  <dd className="text-xl font-bold text-[#10B981] mt-1">Offline</dd>
                </div>
              </dl>

              <div className="mt-6 space-y-3 text-sm text-[#8E9BB0] leading-relaxed">
                <p className="font-semibold text-[#F8FAFC]">
                  O que você precisa autorizar no Android após baixar:
                </p>
                <ol className="space-y-2 list-decimal list-inside">
                  <li>
                    <strong className="text-[#F1F5F9]">Instalação de APK:</strong> ao tocar no arquivo baixado, permita instalar aplicativos desta fonte (navegador ou gerenciador de arquivos) nas configurações do Android.
                  </li>
                  <li>
                    <strong className="text-[#F1F5F9]">Serviço de Acessibilidade (opcional):</strong> para confirmação assistida da fila, ative explicitamente o serviço do DesinstalaJá em <em>Configurações → Acessibilidade</em>.
                  </li>
                </ol>
              </div>
            </div>

            <div className="pt-2 flex flex-wrap items-center gap-4">
              {DOWNLOAD_AVAILABLE ? (
                <a
                  href={OFFICIAL_DOWNLOAD_URL}
                  className="group btn-lift cta-glow-spin flex w-full items-center justify-center whitespace-nowrap focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
                >
                  <span className="relative z-10 flex w-full items-center justify-center gap-2.5 px-[22px] py-[12px] rounded-[8px] text-base font-semibold text-[#04060A] bg-[#F59E0B] group-hover:bg-[#FBBF24] transition-colors">
                    <ArrowDownToLine className="w-5 h-5" aria-hidden="true" />
                    <span>Baixar APK oficial (v{APP_FACTS.version})</span>
                  </span>
                </a>
              ) : (
                <button
                  type="button"
                  disabled
                  aria-describedby="apk-unavailable-notice"
                  className="flex w-full cursor-not-allowed items-center justify-center whitespace-nowrap rounded-lg border border-[#1A2338] px-[22px] py-[12px] text-base font-semibold text-[#8E9BB0]"
                >
                  Download temporariamente indisponível
                </button>
              )}
              {!DOWNLOAD_AVAILABLE && (
                <p id="apk-unavailable-notice" className="text-sm text-[#8E9BB0]" role="status">
                  O download direto não está disponível no momento. Tente novamente mais tarde.
                </p>
              )}
            </div>
          </div>

          {/* Right: Real QR Code pointing to https://desinstalador-android.vercel.app */}
          <aside className="hidden lg:col-span-5 lg:flex bg-[#0F1523] border border-[#1A2338] rounded-xl p-6 sm:p-8 flex-col items-center justify-between text-center gap-5 alt-enter-right">
            <div>
              <h3 className="text-lg font-semibold text-[#F8FAFC]">Abrir no celular via QR Code</h3>
              <p className="text-xs sm:text-sm text-[#8E9BB0] mt-1">
                Se você está no computador, aponte a câmera do Android para abrir o endereço oficial e baixar o APK direto no aparelho.
              </p>
            </div>

            {/* Scannable high-contrast QR code */}
            <div className="p-4 bg-[#F8FAFC] rounded-xl border border-[#1A2338] inline-block">
              <svg
                width="168"
                height="168"
                viewBox={`0 0 ${qr.size + 4} ${qr.size + 4}`}
                role="img"
                aria-label={`QR Code apontando para ${OFFICIAL_SITE_URL}`}
                shapeRendering="crispEdges"
              >
                <rect width={qr.size + 4} height={qr.size + 4} fill="#F8FAFC" />
                {qr.modules.map((row, rIdx) =>
                  row.map((dark, cIdx) =>
                    dark ? (
                      <rect
                        key={`${rIdx}-${cIdx}`}
                        x={cIdx + 2}
                        y={rIdx + 2}
                        width={1}
                        height={1}
                        fill="#04060A"
                      />
                    ) : null
                  )
                )}
              </svg>
            </div>

            <div className="w-full">
              <div className="flex items-center justify-between gap-2 px-3 py-2.5 rounded-lg bg-[#04060A] border border-[#1A2338]">
                <input
                  type="text"
                  readOnly
                  value={OFFICIAL_SITE_URL}
                  aria-label="Endereço oficial do site"
                  onFocus={(event) => event.currentTarget.select()}
                  className="min-w-0 flex-1 bg-transparent font-mono text-[11px] text-[#F1F5F9] outline-none"
                />
                <button
                  type="button"
                  onClick={handleCopySiteUrl}
                  aria-label="Copiar link do site oficial"
                  className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#F59E0B] hover:text-[#FBBF24] whitespace-nowrap shrink-0 cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
                >
                  {copyState === 'copied' ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-[#10B981]" aria-hidden="true" />
                      <span className="text-[#10B981]">Copiado</span>
                    </>
                  ) : copyState === 'error' ? (
                    <span className="text-[#F59E0B]">Falha ao copiar</span>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" aria-hidden="true" />
                      <span>Copiar</span>
                    </>
                  )}
                </button>
              </div>
              <p role="status" aria-live="polite" className="sr-only">
                {copyState === 'copied'
                  ? 'Endereço copiado.'
                  : copyState === 'error'
                    ? 'Não foi possível copiar. Selecione o endereço e copie manualmente.'
                    : ''}
              </p>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
