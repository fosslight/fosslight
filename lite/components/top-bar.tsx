import Logo from '@/public/images/logo.png';
import Image from 'next/image';

export default function TopBar() {
  return (
    <div className="flex justify-between items-center h-12 px-4">
      <div className="relative w-6 h-6">
        <Image src={Logo} fill sizes="48px" alt="fosslight" />
      </div>
      <div className="text-xl font-semibold">
        FOSSLight Hub&nbsp;
        <span className="text-lg font-light">Lite</span>
      </div>
      <i className="text-lg fa-solid fa-user"></i>
    </div>
  );
}
