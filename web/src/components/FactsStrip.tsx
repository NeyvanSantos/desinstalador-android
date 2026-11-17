import { CheckCircle2, HardDrive, ShieldCheck, Smartphone, WifiOff } from 'lucide-react';
import { APP_FACTS } from '../utils/desinstalaUtils';
import { useScrollReveal } from '../utils/useScrollReveal';

const FACT_SHORTCUTS = [
  {
    href: '#download',
    icon: Smartphone,
    title: 'Android 8+',
    detail: 'Versão 8.0 ou superior',
  },
  {
    href: '#download',
    icon: HardDrive,
    title: `APK v${APP_FACTS.version}`,
    detail: APP_FACTS.sizeLabel,
  },
  {
    href: '#seguranca',
    icon: WifiOff,
    title: 'Offline',
    detail: 'Sem acesso à Internet',
  },
  {
    href: '#como-funciona',
    icon: ShieldCheck,
    title: 'Sem root',
    detail: 'Não exige PC nem cabo',
  },
  {
    href: '#como-funciona',
    icon: CheckCircle2,
    title: 'Diálogos nativos',
    detail: 'Confirmação assistida',
  },
];

export default function FactsStrip() {
  const { ref, visible } = useScrollReveal();

  return (
    <section
      aria-label="Fatos técnicos e atalhos rápidos"
      className="bg-[#0A0E17] border-b border-[#1A2338]"
    >
      <div
        ref={ref}
        className={`max-w-6xl mx-auto px-4 sm:px-6 scroll-reveal ${visible ? 'is-visible' : ''}`}
      >
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 divide-y sm:divide-y-0 sm:divide-x divide-[#1A2338]">
          {FACT_SHORTCUTS.map((item) => {
            const Icon = item.icon;
            return (
              <a
                key={item.title}
                href={item.href}
                className="group flex items-center gap-3 py-4 px-3 sm:px-4 hover:bg-[#0F1523] transition-colors focus-visible:outline-2 focus-visible:outline-offset-[-2px] focus-visible:outline-[#F59E0B]"
              >
                <Icon
                  className="w-5 h-5 text-[#F59E0B] shrink-0 group-hover:scale-105 transition-transform"
                  aria-hidden="true"
                />
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-[#F8FAFC] tabular-nums whitespace-nowrap">
                    {item.title}
                  </p>
                  <p className="text-xs text-[#8E9BB0] truncate">{item.detail}</p>
                </div>
              </a>
            );
          })}
        </div>
      </div>
    </section>
  );
}
