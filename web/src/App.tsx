import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import FactsStrip from './components/FactsStrip';
import HowItWorksSteps from './components/HowItWorksSteps';
import EditorialSecurity from './components/EditorialSecurity';
import DownloadSection from './components/DownloadSection';
import FaqSection from './components/FaqSection';
import ClosingCtaAndFooter from './components/ClosingCtaAndFooter';

export default function App() {
  return (
    <div className="min-h-screen flex flex-col bg-[#04060A] text-[#F1F5F9]">
      <Navbar />
      <main className="flex-1">
        <HeroSection />
        <FactsStrip />
        <HowItWorksSteps />
        <EditorialSecurity />
        <DownloadSection />
        <FaqSection />
      </main>
      <ClosingCtaAndFooter />
    </div>
  );
}
