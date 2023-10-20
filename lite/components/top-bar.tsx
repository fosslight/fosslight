import Logo from '@/public/images/logo.png';
import Image from 'next/image';
import Link from 'next/link';

export default function TopBar() {
  return (
    <div className="flex justify-between items-center h-12 px-4">
      <Link className="relative w-6 h-6 no-tap-highlight" href="/">
        <Image src={Logo} fill sizes="48px" alt="fosslight" />
      </Link>
      <Link className="text-xl font-semibold no-tap-highlight" href="/">
        FOSSLight Hub&nbsp;
        <span className="text-lg font-light">Lite</span>
      </Link>
      <i className="text-lg fa-solid fa-user"></i>
    </div>
  );
}
