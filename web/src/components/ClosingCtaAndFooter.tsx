import { useEffect, useRef, useState } from 'react';
import { ArrowDownToLine, ArrowRight, FileText, ShieldCheck, X } from 'lucide-react';
import BrandMark from './BrandMark';
import {
  APP_FACTS,
  DOWNLOAD_AVAILABLE,
  OFFICIAL_DOWNLOAD_URL,
} from '../utils/desinstalaUtils';
import { useScrollReveal } from '../utils/useScrollReveal';

type LegalModalType = 'terms' | 'privacy' | null;

export default function ClosingCtaAndFooter() {
  const { ref, visible } = useScrollReveal();
  const [legalModal, setLegalModal] = useState<LegalModalType>(null);
  const legalDialogRef = useRef<HTMLDivElement>(null);
  const restoreFocusRef = useRef<HTMLElement | null>(null);
  const isLegalModalOpen = legalModal !== null;

  useEffect(() => {
    if (!legalModal) return;
    restoreFocusRef.current =
      document.activeElement instanceof HTMLElement ? document.activeElement : null;
    const dialog = legalDialogRef.current;
    const focusableSelector =
      'a[href], button:not([disabled]), input:not([disabled]), [tabindex]:not([tabindex="-1"])';
    const focusableElements = dialog
      ? Array.from(dialog.querySelectorAll<HTMLElement>(focusableSelector))
      : [];
    focusableElements[0]?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setLegalModal(null);
        return;
      }
      if (event.key !== 'Tab' || !dialog) return;

      const currentFocusableElements =
        Array.from(dialog.querySelectorAll<HTMLElement>(focusableSelector));
      const first = currentFocusableElements[0];
      const last = currentFocusableElements.at(-1);
      if (!first || !last) {
        event.preventDefault();
        dialog.focus();
      } else if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      restoreFocusRef.current?.focus();
      restoreFocusRef.current = null;
    };
  }, [isLegalModalOpen]);

  return (
    <>
      <section className="relative overflow-hidden py-16 sm:py-24 oled-atmosphere border-b border-[#1A2338]">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -bottom-24 left-[15%] w-[360px] h-[360px] rounded-full bg-[#10B981]/8 blur-[110px] ambient-orb"
        />
        <div
          ref={ref}
          className={`relative max-w-4xl mx-auto px-4 sm:px-6 text-center scroll-reveal-alt ${visible ? 'is-visible' : ''}`}
        >
          <div className="alt-enter-scale">
          <p className="text-sm font-medium text-[#10B981] mb-3">
            Pronto para organizar seu Android
          </p>
          <h2 className="text-3xl sm:text-5xl font-bold tracking-tight text-[#F8FAFC] text-balance mb-5">
            Baixe o DesinstalaJá e limpe aplicativos em fila.
          </h2>
          <p className="text-base text-[#8E9BB0] max-w-2xl mx-auto leading-relaxed mb-8">
            Versão {APP_FACTS.version} para {APP_FACTS.minAndroid}. APK oficial assinado: {APP_FACTS.sizeLabel}.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-4">
            {DOWNLOAD_AVAILABLE ? (
              <a
                href={OFFICIAL_DOWNLOAD_URL}
                className="group btn-lift cta-glow-spin inline-flex items-center justify-center whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
              >
                <span className="relative z-10 inline-flex items-center justify-center gap-2.5 px-[26px] py-[14px] rounded-[8px] text-base font-semibold text-[#04060A] bg-[#F59E0B] group-hover:bg-[#FBBF24] transition-colors">
                  <ArrowDownToLine className="w-5 h-5" aria-hidden="true" />
                  <span>Baixar APK oficial</span>
                </span>
              </a>
            ) : (
              <a
                href="#download"
                className="inline-flex items-center justify-center whitespace-nowrap rounded-lg border border-[#1A2338] px-[26px] py-[14px] text-base font-semibold text-[#8E9BB0]"
              >
                Download indisponível
              </a>
            )}

            <a
              href="#faq"
              className="group btn-lift inline-flex items-center justify-center gap-2 px-5 py-4 text-sm font-semibold text-[#F1F5F9] hover:text-[#F59E0B] border border-[#1A2338] hover:border-[#F59E0B]/60 bg-[#0A0E17] rounded-lg whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
            >
              <span>Ler dúvidas sobre acessibilidade</span>
              <ArrowRight className="w-4 h-4 arrow-shift" aria-hidden="true" />
            </a>
          </div>
          </div>
        </div>
      </section>

      <footer className="bg-[#04060A] py-10">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div>
            <a
              href="#topo"
              className="inline-flex items-center gap-2.5 text-base font-bold tracking-tight text-[#F8FAFC] whitespace-nowrap"
              style={{ fontFamily: 'var(--font-display)' }}
            >
              <BrandMark className="w-6 h-6 shrink-0" />
              <span>DesinstalaJá</span>
            </a>
            <p className="text-xs text-[#8E9BB0] mt-2">
              Versão {APP_FACTS.version} · © {new Date().getFullYear()} Neyvan Santos
            </p>
          </div>

          <nav aria-label="Links do rodapé" className="flex flex-wrap items-center gap-6 text-sm text-[#8E9BB0]">
            <a href="#como-funciona" className="nav-link-underline py-1 hover:text-[#F8FAFC] whitespace-nowrap">
              Como funciona
            </a>
            <a href="#seguranca" className="nav-link-underline py-1 hover:text-[#F8FAFC] whitespace-nowrap">
              Segurança
            </a>
            <a href="#faq" className="nav-link-underline py-1 hover:text-[#F8FAFC] whitespace-nowrap">
              FAQ
            </a>
            <button
              type="button"
              onClick={() => setLegalModal('terms')}
              className="nav-link-underline py-1 hover:text-[#F8FAFC] whitespace-nowrap cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-[#F59E0B] rounded-sm"
            >
              Termos de uso
            </button>
            <button
              type="button"
              onClick={() => setLegalModal('privacy')}
              className="nav-link-underline py-1 hover:text-[#F8FAFC] whitespace-nowrap cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-[#F59E0B] rounded-sm"
            >
              Privacidade
            </button>
          </nav>
        </div>
      </footer>

      {legalModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#04060A]/80 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="legal-modal-title"
          aria-describedby="legal-modal-description"
          onClick={() => setLegalModal(null)}
        >
          <div
            ref={legalDialogRef}
            tabIndex={-1}
            className="w-full max-w-xl max-h-[85vh] overflow-y-auto rounded-2xl border border-[#1A2338] bg-[#0A0E17] p-6 sm:p-7 shadow-2xl text-left"
            onClick={(event) => event.stopPropagation()}
          >
            <p id="legal-modal-description" className="sr-only">
              Informações legais do aplicativo DesinstalaJá. Use Escape para fechar esta janela.
            </p>
            <div className="flex items-start justify-between gap-4 pb-4 border-b border-[#1A2338]">
              <div className="flex items-center gap-2.5">
                {legalModal === 'terms' ? (
                  <FileText className="w-5 h-5 text-[#F59E0B] shrink-0" aria-hidden="true" />
                ) : (
                  <ShieldCheck className="w-5 h-5 text-[#10B981] shrink-0" aria-hidden="true" />
                )}
                <h2 id="legal-modal-title" className="text-xl font-bold text-[#F8FAFC]">
                  {legalModal === 'terms' ? 'Termos de Uso — DesinstalaJá' : 'Política de Privacidade — DesinstalaJá'}
                </h2>
              </div>
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                aria-label="Fechar janela"
                className="inline-flex items-center justify-center w-9 h-9 rounded-lg border border-[#1A2338] bg-[#0F1523] text-[#8E9BB0] hover:text-[#F8FAFC] hover:border-[#F59E0B]/60 transition-colors cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            {legalModal === 'terms' ? (
              <div className="mt-4 space-y-3 text-sm text-[#8E9BB0] leading-relaxed">
                <p>
                  <strong className="text-[#F8FAFC]">Finalidade:</strong> o DesinstalaJá é um utilitário gratuito para {APP_FACTS.minAndroid} que organiza a remoção sequencial de aplicativos usando exclusivamente os diálogos oficiais do instalador do sistema.
                </p>
                <p>
                  <strong className="text-[#F8FAFC]">Responsabilidade pela seleção:</strong> desinstalar um aplicativo apaga os dados locais associados a ele no aparelho, exatamente como na remoção manual do Android. Não há promessa de risco zero; revise os itens marcados antes de iniciar a fila.
                </p>
                <p>
                  <strong className="text-[#F8FAFC]">Acessibilidade e limites:</strong> a confirmação assistida dos diálogos requer ativação explícita pelo usuário nas configurações do Android e conta com proteções contra pacotes críticos do sistema e interrupção diante de telas personalizadas pelo fabricante.
                </p>
                <p>
                  <strong className="text-[#F8FAFC]">Distribuição oficial:</strong> o APK 1.2 assinado está disponível nesta página. Verifique a versão e o nome do app antes de instalar.
                </p>
              </div>
            ) : (
              <div className="mt-4 space-y-3 text-sm text-[#8E9BB0] leading-relaxed">
                <p>
                  <strong className="text-[#F8FAFC]">Funcionamento 100% offline:</strong> o aplicativo DesinstalaJá não declara permissão de Internet (<code className="font-mono text-xs text-[#F1F5F9]">INTERNET</code>) no manifesto do Android e não realiza conexões de rede.
                </p>
                <p>
                  <strong className="text-[#F8FAFC]">Processamento local:</strong> o inventário de aplicativos e o texto da janela do instalador são processados no aparelho; o texto da janela não é armazenado nem transmitido. Os identificadores dos apps selecionados e o progresso da fila são mantidos em armazenamento privado para permitir recuperação após encerramento do processo. Ao concluir ou cancelar, o estado da fila é removido; a seleção restante permanece local até ser alterada.
                </p>
                <p>
                  <strong className="text-[#F8FAFC]">Uso restrito da Acessibilidade:</strong> quando habilitado manualmente por você, o serviço (<code className="font-mono text-xs text-[#F1F5F9]">BIND_ACCESSIBILITY_SERVICE</code>) atua somente durante uma fila iniciada pelo usuário para reconhecer e confirmar o diálogo nativo de desinstalação, sem registrar nem armazenar informações de tela.
                </p>
                <p>
                  <strong className="text-[#F8FAFC]">Este site:</strong> página estática informativa, sem anúncios ou cookies de rastreamento. O APK 1.2 é hospedado neste domínio.
                </p>
              </div>
            )}

            <div className="mt-6 pt-4 border-t border-[#1A2338] flex items-center justify-between gap-4">
              <div className="flex items-center gap-4 text-xs">
                <button
                  type="button"
                  onClick={() => setLegalModal(legalModal === 'terms' ? 'privacy' : 'terms')}
                  className="text-[#F59E0B] hover:text-[#FBBF24] font-semibold cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
                >
                  {legalModal === 'terms' ? 'Ver Privacidade' : 'Ver Termos de Uso'}
                </button>
              </div>
              <button
                type="button"
                onClick={() => setLegalModal(null)}
                className="px-4 py-2 text-sm font-semibold text-[#04060A] bg-[#F59E0B] hover:bg-[#FBBF24] rounded-lg transition-colors cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
