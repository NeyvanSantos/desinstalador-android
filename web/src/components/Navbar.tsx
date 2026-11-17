import { useState } from 'react';
import { Menu, X } from 'lucide-react';
import BrandMark from './BrandMark';
import { OFFICIAL_DOWNLOAD_URL } from '../utils/desinstalaUtils';

const NAV_LINKS = [
  { href: '#como-funciona', label: 'Como funciona' },
  { href: '#seguranca', label: 'Segurança' },
  { href: '#download', label: 'Download' },
  { href: '#faq', label: 'FAQ' },
];

export default function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 liquid-glass border-b border-[#1A2338]">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4 sm:gap-8">
        {/* Zone 1: Logo + single-line brand title */}
        <a
          href="#topo"
          className="inline-flex items-center gap-2.5 text-lg font-bold tracking-tight text-[#F8FAFC] whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-[#F59E0B] rounded-sm"
          style={{ fontFamily: 'var(--font-display)' }}
        >
          <BrandMark className="w-7 h-7 shrink-0" />
          <span>DesinstalaJá</span>
        </a>

        {/* Zone 2: Short single-line nav links */}
        <nav aria-label="Navegação principal" className="hidden md:flex items-center gap-8">
          {NAV_LINKS.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className="nav-link-underline nav-link-rise py-1 text-[15px] font-medium tracking-[0.015em] text-[#A3B1C6] hover:text-[#F8FAFC] whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-[#F59E0B] rounded-sm"
              style={{ fontFamily: 'var(--font-display)' }}
            >
              {link.label}
            </a>
          ))}
        </nav>

        {/* Zone 3: Primary action + mobile toggle */}
        <div className="flex items-center gap-3 shrink-0">
          <a
            href={OFFICIAL_DOWNLOAD_URL}
            rel="noopener noreferrer"
            className="group btn-lift cta-glow-spin hidden sm:inline-flex items-center justify-center whitespace-nowrap shrink-0 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
          >
            <span className="relative z-10 inline-flex items-center justify-center px-[14px] py-[6px] rounded-[6px] text-sm font-semibold text-[#04060A] bg-[#F59E0B] group-hover:bg-[#FBBF24] transition-colors">
              Baixar APK
            </span>
          </a>

          <button
            type="button"
            onClick={() => setMobileOpen((prev) => !prev)}
            aria-label={mobileOpen ? 'Fechar menu de navegação' : 'Abrir menu de navegação'}
            aria-expanded={mobileOpen}
            className="md:hidden inline-flex items-center justify-center w-11 h-11 rounded-lg border border-[#1A2338] bg-[#0F1523] text-[#F1F5F9] hover:border-[#F59E0B]/60 transition-colors cursor-pointer focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#F59E0B]"
          >
            {mobileOpen ? <X className="w-5 h-5" aria-hidden="true" /> : <Menu className="w-5 h-5" aria-hidden="true" />}
          </button>
        </div>
      </div>

      {mobileOpen && (
        <nav
          aria-label="Navegação móvel"
          className="md:hidden border-t border-[#1A2338] bg-[#0A0E17] px-4 py-3 flex flex-col gap-1"
        >
          {NAV_LINKS.map((link) => (
            <a
              key={link.href}
              href={link.href}
              onClick={() => setMobileOpen(false)}
              className="px-3 py-2.5 text-sm font-medium text-[#8E9BB0] hover:text-[#F8FAFC] hover:bg-[#0F1523] rounded-md whitespace-nowrap"
            >
              {link.label}
            </a>
          ))}
        </nav>
      )}
    </header>
  );
}
