'use client';

import FullSearchBar from '@/components/full-search-bar';
import SideBar from '@/components/side-bar';
import { useState } from 'react';

export default function Layout({ children }: { children: React.ReactNode }) {
  const [isSideBarShown, setIsSideBarShown] = useState(true);

  return (
    <main className="flex min-h-screen">
      <SideBar isShown={isSideBarShown} />
      <div className="flex-1 px-4 pb-8">
        <div className="sticky top-0 flex flex-col gap-y-10 pt-4 pb-8 bg-white">
          <button
            className="w-6 h-6 text-xl text-charcol"
            onClick={() => setIsSideBarShown(!isSideBarShown)}
          >
            <i className="fa-solid fa-bars"></i>
          </button>
          <FullSearchBar />
        </div>
        {children}
      </div>
    </main>
  );
}
