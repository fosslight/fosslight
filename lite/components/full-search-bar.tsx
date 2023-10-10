'use client';

import { viewState } from '@/lib/atoms';
import clsx from 'clsx';
import { useRecoilValue } from 'recoil';

export default function FullSearchBar() {
  const view = useRecoilValue(viewState);
  const placeholder = 'Search by name of OSS, License, or Project';

  // Wait until detecting appropriate view
  if (view === 'none') return null;

  return (
    <div
      className={clsx(
        'flex gap-x-4 px-4 py-3 bg-semiwhite',
        view === 'pc'
          ? 'rounded shadow-[4px_4px_6px_0_rgba(0,0,0,0.3)] focus-within:shadow-[4px_4px_6px_0_rgb(52,57,63,0.7)]'
          : 'shadow-[0_2px_2px_0_rgba(0,0,0,0.2)] focus-within:shadow-[0_2px_2px_0_rgba(52,57,63,0.7)]'
      )}
    >
      <input
        className={clsx(
          'flex-1 bg-transparent outline-none font-semibold',
          view === 'pc' && 'text-lg'
        )}
        placeholder={placeholder}
      />
      <i className="text-2xl text-darkgray fa-solid fa-magnifying-glass"></i>
    </div>
  );
}
