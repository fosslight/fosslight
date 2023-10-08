import Logo from '@/public/images/logo.png';
import Image from 'next/image';

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <main className="flex flex-col justify-center items-center gap-y-6 min-h-screen py-8 bg-charcol">
      <div className="flex items-center gap-x-3">
        <div className="w-10 h-10 p-2 bg-white rounded-full">
          <Image src={Logo} width={24} height={24} alt="fosslight" />
        </div>
        <h1 className="text-2xl text-semiwhite">
          FOSSLight Hub <span className="text-xl font-light">Lite</span>
        </h1>
      </div>
      <div className="w-96 max-w-full px-4 mx-auto">{children}</div>
    </main>
  );
}
