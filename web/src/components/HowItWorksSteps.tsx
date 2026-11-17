import { useState } from 'react';
import { Check, Layers, ListChecks, ShieldAlert } from 'lucide-react';
import { WORKFLOW_STEPS } from '../utils/desinstalaUtils';
import { useScrollReveal } from '../utils/useScrollReveal';

export default function HowItWorksSteps() {
  const [activeStep, setActiveStep] = useState<1 | 2 | 3>(1);
  const { ref, visible } = useScrollReveal();

  const current = WORKFLOW_STEPS.find((step) => step.id === activeStep) ?? WORKFLOW_STEPS[0];

  return (
    <section
      id="como-funciona"
      className="overflow-x-hidden py-16 sm:py-24 bg-[#04060A] border-b border-[#1A2338]"
    >
      <div
        ref={ref}
        className={`max-w-6xl mx-auto px-4 sm:px-6 scroll-reveal-alt ${visible ? 'is-visible' : ''}`}
      >
        <div className="max-w-2xl mb-10 sm:mb-14 alt-enter-head">
          <p className="text-sm font-medium text-[#F59E0B] mb-2">Fluxo em três passos</p>
          <h2 className="text-2xl sm:text-4xl font-bold tracking-tight text-[#F8FAFC] text-balance">
            Como a fila de desinstalação funciona no Android.
          </h2>
          <p className="text-sm sm:text-base text-[#8E9BB0] leading-relaxed mt-3">
            O DesinstalaJá não substitui o instalador do sistema. Ele organiza seus itens em fila e, se você habilitar a acessibilidade, confirma os diálogos oficiais um por um.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
          {/* Left: Interactive 3-step selector */}
          <div className="lg:col-span-6 flex flex-col gap-3 alt-enter-left" role="group" aria-label="Etapas do funcionamento">
            {WORKFLOW_STEPS.map((step) => {
              const selected = step.id === activeStep;
              return (
                <button
                  key={step.id}
                  type="button"
                  aria-pressed={selected}
                  onClick={() => setActiveStep(step.id)}
                  className={`btn-lift text-left p-5 sm:p-6 rounded-xl border transition-colors cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B] ${
                    selected
                      ? 'bg-[#0F1523] border-[#F59E0B]/70'
                      : 'bg-[#0A0E17] border-[#1A2338] hover:border-[#8E9BB0]/40'
                  }`}
                >
                  <div className="flex items-baseline justify-between gap-3 mb-2">
                    <span className="font-mono text-xs font-semibold text-[#F59E0B] tabular-nums">
                      Passo {step.indexLabel}
                    </span>
                    <span className="text-xs text-[#8E9BB0]">{step.shortLabel}</span>
                  </div>

                  <h3 className="text-lg font-semibold text-[#F8FAFC] mb-2">{step.title}</h3>
                  <p className="text-sm text-[#8E9BB0] leading-relaxed">{step.description}</p>
                  {selected && (
                    <p className="text-xs text-[#10B981] mt-3 pt-3 border-t border-[#1A2338] leading-relaxed">
                      {step.detailNote}
                    </p>
                  )}
                </button>
              );
            })}
          </div>

          {/* Right: Conceptual step illustration */}
          <div className="lg:col-span-6 bg-[#0A0E17] border border-[#1A2338] rounded-xl p-6 sm:p-8 flex flex-col justify-between alt-enter-right">
            <div>
              <div className="flex items-center justify-between gap-4 pb-4 border-b border-[#1A2338] mb-6">
                <div>
                  <p className="font-mono text-xs text-[#F59E0B] tabular-nums">
                    Etapa {current.indexLabel} de 03
                  </p>
                  <h3 className="text-lg font-semibold text-[#F8FAFC] mt-0.5">
                    {current.shortLabel}
                  </h3>
                </div>
                <span className="text-xs text-[#8E9BB0]">Ilustração esquemática</span>
              </div>

              {activeStep === 1 && (
                <div className="space-y-3">
                  <div className="p-4 rounded-lg border border-[#F59E0B]/50 bg-[#0F1523] flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <ListChecks className="w-5 h-5 text-[#F59E0B]" aria-hidden="true" />
                      <div>
                        <p className="text-sm font-semibold text-[#F8FAFC]">Jogo Arcade Clássico</p>
                        <p className="font-mono text-xs text-[#8E9BB0]">com.exemplo.arcade</p>
                      </div>
                    </div>
                    <span className="text-xs font-semibold text-[#F59E0B]">Marcado</span>
                  </div>

                  <div className="p-4 rounded-lg border border-[#F59E0B]/50 bg-[#0F1523] flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <ListChecks className="w-5 h-5 text-[#F59E0B]" aria-hidden="true" />
                      <div>
                        <p className="text-sm font-semibold text-[#F8FAFC]">Leitor de Ofertas</p>
                        <p className="font-mono text-xs text-[#8E9BB0]">com.exemplo.ofertas</p>
                      </div>
                    </div>
                    <span className="text-xs font-semibold text-[#F59E0B]">Marcado</span>
                  </div>

                  <div className="p-4 rounded-lg border border-[#1A2338] bg-[#04060A] flex items-center justify-between opacity-75">
                    <div className="flex items-center gap-3">
                      <ShieldAlert className="w-5 h-5 text-[#8E9BB0]" aria-hidden="true" />
                      <div>
                        <p className="text-sm font-semibold text-[#F8FAFC]">Pacotes críticos do Android</p>
                        <p className="font-mono text-xs text-[#8E9BB0]">com.android.systemui</p>
                      </div>
                    </div>
                    <span className="text-xs text-[#8E9BB0]">Protegido</span>
                  </div>
                </div>
              )}

              {activeStep === 2 && (
                <div className="space-y-4">
                  <div className="p-5 rounded-lg border border-[#1A2338] bg-[#0F1523]">
                    <div className="flex items-center justify-between mb-3">
                      <div className="flex items-center gap-2.5">
                        <Layers className="w-5 h-5 text-[#F59E0B]" aria-hidden="true" />
                        <span className="text-sm font-semibold text-[#F8FAFC]">Fila sequencial ativa</span>
                      </div>
                      <span className="font-mono text-xs text-[#F59E0B] tabular-nums">1 / 15</span>
                    </div>
                    <div className="w-full h-2 rounded-full bg-[#04060A] overflow-hidden border border-[#1A2338]">
                      <div className="h-full w-1/3 bg-[#F59E0B] rounded-full" />
                    </div>
                    <p className="text-xs text-[#8E9BB0] mt-3 leading-relaxed">
                      Cada item dispara a intent padrão de desinstalação do Android até concluir toda a lista ou até você interromper.
                    </p>
                  </div>

                  <div className="p-4 rounded-lg border border-[#1A2338] bg-[#04060A] text-xs text-[#8E9BB0] leading-relaxed">
                    Em testes práticos, uma fila de 15 aplicativos leva em torno de 12 segundos — número aproximado que depende do desempenho do aparelho e do tempo dos diálogos do sistema.
                  </div>
                </div>
              )}

              {activeStep === 3 && (
                <div className="space-y-4">
                  <div className="p-5 rounded-xl border border-[#1A2338] bg-[#0F1523]">
                    <p className="text-xs text-[#8E9BB0] mb-1">Diálogo oficial do instalador Android</p>
                    <p className="text-base font-semibold text-[#F8FAFC] mb-2">
                      Deseja desinstalar este app?
                    </p>
                    <p className="text-xs text-[#8E9BB0] mb-5">
                      O serviço de acessibilidade identifica apenas a janela do instalador do Android durante uma fila iniciada por você e toca no botão de confirmação.
                    </p>
                    <div className="flex items-center justify-end gap-3">
                      <span className="px-3.5 py-2 text-xs font-medium text-[#8E9BB0] border border-[#1A2338] rounded-lg">
                        Cancelar
                      </span>
                      <span className="px-4 py-2 text-xs font-semibold text-[#04060A] bg-[#10B981] rounded-lg inline-flex items-center gap-1.5">
                        <Check className="w-4 h-4" aria-hidden="true" />
                        OK · Confirmado
                      </span>
                    </div>
                  </div>

                  <p className="text-xs text-[#8E9BB0] leading-relaxed">
                    Importante: não há desinstalação silenciosa oculta. Você precisa habilitar manualmente o serviço de acessibilidade nas configurações do aparelho antes de usar a confirmação assistida.
                  </p>
                </div>
              )}
            </div>

            <div className="mt-6 pt-4 border-t border-[#1A2338] flex items-center justify-between text-xs text-[#8E9BB0]">
              <span>Ilustração conceitual</span>
              <span>Fluxo nativo Android 8.0+</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
