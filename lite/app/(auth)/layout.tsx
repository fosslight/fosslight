import Logo from '@/public/images/logo.png';
import Image from 'next/image';

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <main className="flex flex-col justify-center items-center gap-y-6 min-h-screen py-8 bg-charcoal">
      <div className="flex items-center gap-x-3">
        <div className="w-10 h-10 p-2 bg-white rounded-full">
          <div className="relative w-6 h-6">
            <Image src={Logo} fill sizes="48px" alt="fosslight" />
          </div>
        </div>
        <h1 className="text-2xl text-semiwhite">
          FOSSLight Hub <span className="text-xl font-light">Lite</span>
        </h1>
      </div>
      <div className="w-96 max-w-full px-4 mx-auto">{children}</div>
    </main>
  );
}
