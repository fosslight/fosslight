'use client';

import { viewState } from '@/lib/atoms';
import { useRecoilValue } from 'recoil';

export default function FullSearchBar() {
  const view = useRecoilValue(viewState);
  const placeholder = 'Search by name of OSS, License, or Project';

  // Wait until detecting appropriate view
  if (view === 'none') return null;

  // Mobile View (≤ 768px)
  if (view === 'mobile') {
    return (
      <div className="flex gap-x-4 px-4 py-3 bg-semiwhite">
        <input
          className="flex-1 bg-transparent outline-none font-semibold"
          placeholder={placeholder}
        />
        <i className="text-xl text-darkgray fa-solid fa-magnifying-glass"></i>
      </div>
    );
  }

  // PC View (> 768px)
  return (
    <div className="flex gap-x-4 px-4 py-3 bg-semiwhite rounded shadow-[4px_4px_6px_0_rgba(0,0,0,0.3)] focus-within:shadow-[4px_4px_6px_0_rgb(52,57,63,0.8)]">
      <input
        className="flex-1 bg-transparent outline-none text-lg font-semibold"
        placeholder={placeholder}
      />
      <i className="text-2xl text-darkgray fa-solid fa-magnifying-glass"></i>
    </div>
  );
}
