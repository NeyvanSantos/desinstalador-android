import { useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { useScrollReveal } from '../utils/useScrollReveal';

const FAQ_ITEMS = [
  {
    question: 'Por que preciso habilitar o Serviço de Acessibilidade manualmente?',
    answer:
      'O Android bloqueia por segurança que qualquer aplicativo ative acessibilidade sozinho. Quando você habilita explicitamente o serviço do DesinstalaJá nas configurações do sistema, ele consegue ler o botão de confirmação dos diálogos oficiais do instalador durante uma fila ativa — e você pode desativá-lo a qualquer momento.',
  },
  {
    question: 'O DesinstalaJá realiza desinstalação silenciosa sem diálogos?',
    answer:
      'Não. O aplicativo sempre utiliza os diálogos nativos do instalador do Android. A fila organiza os pedidos em sequência, mas o serviço opcional só confirma quando consegue identificar o instalador, o app e a ação com segurança. Se não conseguir, a confirmação manual continua disponível.',
  },
  {
    question: 'Preciso fazer root ou conectar o celular no computador?',
    answer:
      'Não. Todo o fluxo funciona diretamente no smartphone Android 8.0 ou superior, sem acesso root e sem cabo USB ou comandos ADB.',
  },
  {
    question: 'O aplicativo funciona com quais versões e marcas de Android?',
    answer:
      'O DesinstalaJá é compatível com Android 8.0 ou superior. Em aparelhos cuja fabricante personaliza fortemente o instalador do sistema ou exibe confirmações extras, a fila pode exigir interação manual caso o diálogo não siga o padrão esperado pela automação.',
  },
  {
    question: 'Como instalar o APK oficial baixado pelo site?',
    answer:
      'A versão 1.2 assinada está disponível diretamente nesta página (1,96 MB). Depois do download, abra o arquivo e autorize a instalação para essa fonte nas configurações do Android.',
  },
  {
    question: 'Posso perder dados ou apagar algo importante do sistema?',
    answer:
      'Existem proteções para impedir a seleção de pacotes críticos do Android, mas a desinstalação de qualquer aplicativo escolhido por você remove os dados locais daquele aplicativo definitivamente. Não existe promessa de risco zero: confira os nomes marcados antes de iniciar a fila.',
  },
];

export default function FaqSection() {
  const [openIdx, setOpenIdx] = useState<number | null>(0);
  const { ref, visible } = useScrollReveal();

  return (
    <section id="faq" className="py-16 sm:py-24 bg-[#0A0E17] border-b border-[#1A2338]">
      <div
        ref={ref}
        className={`max-w-4xl mx-auto px-4 sm:px-6 scroll-reveal ${visible ? 'is-visible' : ''}`}
      >
        <div className="mb-10">
          <p className="text-sm font-medium text-[#F59E0B] mb-2">Dúvidas frequentes</p>
          <h2 className="text-2xl sm:text-4xl font-bold tracking-tight text-[#F8FAFC] text-balance">
            Acessibilidade, compatibilidade e instalação do APK.
          </h2>
        </div>

        <div className="rounded-xl border border-[#1A2338] bg-[#0F1523] divide-y divide-[#1A2338] overflow-hidden">
          {FAQ_ITEMS.map((item, idx) => {
            const isOpen = openIdx === idx;
            return (
              <div
                key={item.question}
                className={`faq-item-stagger relative transition-colors ${
                  isOpen ? 'bg-[#12192A]' : 'hover:bg-[#0A0E17]/60'
                }`}
                style={{ transitionDelay: `${idx * 65}ms` }}
              >
                <span
                  aria-hidden="true"
                  className={`absolute left-0 top-0 bottom-0 w-[3px] bg-[#F59E0B] transition-opacity duration-300 ${
                    isOpen ? 'opacity-100' : 'opacity-0'
                  }`}
                />
                <button
                  type="button"
                  onClick={() => setOpenIdx(isOpen ? null : idx)}
                  aria-expanded={isOpen}
                  aria-controls={`faq-answer-${idx}`}
                  className="w-full px-5 sm:px-6 py-4 sm:py-5 text-left flex items-center justify-between gap-4 cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-[-2px] focus-visible:outline-[#F59E0B]"
                >
                  <span
                    className={`text-sm sm:text-base font-semibold transition-colors ${
                      isOpen ? 'text-[#F8FAFC]' : 'text-[#F1F5F9]'
                    }`}
                  >
                    {item.question}
                  </span>
                  <ChevronDown
                    className={`w-5 h-5 shrink-0 transition-transform duration-300 ease-out ${
                      isOpen ? 'rotate-180 text-[#F59E0B]' : 'text-[#8E9BB0]'
                    }`}
                    aria-hidden="true"
                  />
                </button>
                <div
                  id={`faq-answer-${idx}`}
                  aria-hidden={!isOpen}
                  inert={!isOpen}
                  className="faq-accordion-content"
                  data-open={isOpen ? 'true' : 'false'}
                >
                  <div className="faq-accordion-inner">
                    <div className="px-5 sm:px-6 pb-5 text-sm text-[#8E9BB0] leading-relaxed">
                      {item.answer}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
