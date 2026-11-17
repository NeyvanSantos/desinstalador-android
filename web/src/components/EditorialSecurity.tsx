import { AlertTriangle, Lock, ShieldCheck, WifiOff } from 'lucide-react';
import { MANIFEST_PERMISSIONS } from '../utils/desinstalaUtils';
import { useScrollReveal } from '../utils/useScrollReveal';

export default function EditorialSecurity() {
  const { ref, visible } = useScrollReveal();

  return (
    <section id="seguranca" className="py-16 sm:py-24 bg-[#0A0E17] border-b border-[#1A2338]">
      <div
        ref={ref}
        className={`max-w-6xl mx-auto px-4 sm:px-6 space-y-16 scroll-reveal ${visible ? 'is-visible' : ''}`}
      >
        {/* Editorial block 1: Benefits & offline architecture */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
          <div className="lg:col-span-5">
            <p className="text-sm font-medium text-[#10B981] mb-2">Arquitetura offline</p>
            <h2 className="text-2xl sm:text-4xl font-bold tracking-tight text-[#F8FAFC] text-balance">
              Por que o DesinstalaJá não pede acesso à Internet.
            </h2>
          </div>

          <div className="lg:col-span-7 grid grid-cols-1 sm:grid-cols-2 gap-6">
            <article className="p-6 rounded-xl bg-[#0F1523] border border-[#1A2338]">
              <WifiOff className="w-5 h-5 text-[#10B981] mb-3" aria-hidden="true" />
              <h3 className="text-base font-semibold text-[#F8FAFC] mb-2">
                Sem permissão de rede no manifesto
              </h3>
              <p className="text-sm text-[#8E9BB0] leading-relaxed">
                O aplicativo não solicita a permissão <code className="font-mono text-xs text-[#F1F5F9]">INTERNET</code>. A lista dos seus apps instalados nunca sai do aparelho e não há anúncios ou telemetria.
              </p>
            </article>

            <article className="p-6 rounded-xl bg-[#0F1523] border border-[#1A2338]">
              <Lock className="w-5 h-5 text-[#F59E0B] mb-3" aria-hidden="true" />
              <h3 className="text-base font-semibold text-[#F8FAFC] mb-2">
                Proteção contra pacotes críticos
              </h3>
              <p className="text-sm text-[#8E9BB0] leading-relaxed">
                Componentes essenciais do sistema operacional e o próprio instalador de pacotes do Android são bloqueados na seleção para evitar remoção acidental.
              </p>
            </article>
          </div>
        </div>

        {/* Editorial block 2: Permissions & realistic limits */}
        <div className="pt-12 border-t border-[#1A2338] grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
          <div className="lg:col-span-7">
            <p className="text-sm font-medium text-[#F59E0B] mb-2">Transparência técnica</p>
            <h3 className="text-xl sm:text-2xl font-bold text-[#F8FAFC] mb-6">
              Permissões declaradas e para que cada uma serve
            </h3>

            <div className="rounded-xl border border-[#1A2338] bg-[#0F1523] divide-y divide-[#1A2338]">
              {MANIFEST_PERMISSIONS.map((item) => (
                <div key={item.permission} className="p-5 flex items-start gap-3.5">
                  <ShieldCheck className="w-5 h-5 text-[#10B981] shrink-0 mt-0.5" aria-hidden="true" />
                  <div>
                    <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
                      <code className="font-mono text-xs sm:text-sm font-semibold text-[#F8FAFC]">
                        {item.permission}
                      </code>
                      <span className="text-xs text-[#8E9BB0]">{item.required}</span>
                    </div>
                    <p className="text-sm text-[#8E9BB0] leading-relaxed mt-1.5">{item.purpose}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Right: Realistic limits (no "zero risk" claims) */}
          <aside className="lg:col-span-5 p-6 sm:p-7 rounded-xl bg-[#04060A] border border-[#1A2338]">
            <div className="flex items-center gap-2.5 mb-4">
              <AlertTriangle className="w-5 h-5 text-[#F59E0B] shrink-0" aria-hidden="true" />
              <h3 className="text-lg font-semibold text-[#F8FAFC]">
                Limites e cuidados reais
              </h3>
            </div>

            <ul className="space-y-4 text-sm text-[#8E9BB0] leading-relaxed">
              <li>
                <strong className="text-[#F8FAFC] font-semibold">Não promete risco zero:</strong>{' '}
                desinstalar qualquer aplicativo apaga os dados locais dele no aparelho, da mesma forma que a remoção manual nas configurações do Android. Revise sua lista antes de iniciar a fila.
              </li>
              <li>
                <strong className="text-[#F8FAFC] font-semibold">Limites da automação:</strong>{' '}
                o serviço de acessibilidade atua apenas durante uma fila ativa e possui limites para não ficar preso caso um fabricante use um diálogo fora do padrão ou peça senha.
              </li>
              <li>
                <strong className="text-[#F8FAFC] font-semibold">Apps pré-instalados protegidos:</strong>{' '}
                aplicativos gravados na partição do sistema não podem ser removidos sem privilégios administrativos do sistema; nesses casos, o Android exibe apenas opção de desativação ou bloqueio.
              </li>
            </ul>
          </aside>
        </div>
      </div>
    </section>
  );
}
