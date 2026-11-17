interface BrandMarkProps {
  className?: string;
}

export default function BrandMark({ className = 'w-7 h-7' }: BrandMarkProps) {
  return <img src="/logo.png" alt="" className={className} />;
}
