'use client';

import BottomBar from '@/components/bottom-bar';
import FullSearchBar from '@/components/full-search-bar';
import SideBar from '@/components/side-bar';
import Logo from '@/public/images/logo.png';
import Image from 'next/image';
import { useEffect, useState } from 'react';
import { useMediaQuery } from 'react-responsive';

export default function Layout({ children }: { children: React.ReactNode }) {
  const [isSideBarShown, setIsSideBarShown] = useState(true);
  const [view, setView] = useState<'pc' | 'mobile' | 'none'>('none');
  const isMobile = useMediaQuery({ maxWidth: 768 });

  useEffect(() => {
    if (isMobile) setView('mobile');
    else setView('pc');
  }, [isMobile]);

  // Wait until detecting appropriate view
  if (view === 'none') return null;

  // Mobile View (≤ 768px)
  if (view === 'mobile') {
    return (
      <main className="flex flex-col min-h-screen">
        <div className="sticky top-0 flex justify-between items-center h-12 px-4 bg-white shadow-[0_0_3px_2px_rgba(0,0,0,0.2)]">
          <div className="relative w-6 h-6">
            <Image src={Logo} fill sizes="48px" alt="fosslight" />
          </div>
          <div className="text-xl font-semibold">
            FOSSLight Hub&nbsp;
            <span className="text-lg font-light">Lite</span>
          </div>
          <i className="text-lg fa-solid fa-user"></i>
        </div>
        <div className="pt-4 px-4 pb-24">{children}</div>
        <BottomBar />
      </main>
    );
  }

  // PC View (> 768px)
  return (
    <main className="flex min-h-screen">
      <SideBar isShown={isSideBarShown} />
      <div className="flex-1 min-w-0 px-4">
        <div className="sticky top-0 flex flex-col gap-y-10 pt-4 pb-8 bg-white">
          <button
            className="w-6 h-6 text-xl text-charcol"
            onClick={() => setIsSideBarShown(!isSideBarShown)}
          >
            <i className="fa-solid fa-bars"></i>
          </button>
          <FullSearchBar />
        </div>
        <div className="pb-8 overflow-x-auto no-scrollbar">{children}</div>
      </div>
    </main>
  );
}
