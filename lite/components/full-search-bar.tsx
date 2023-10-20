import { viewState } from '@/lib/atoms';
import clsx from 'clsx';
import { useRouter, useSearchParams } from 'next/navigation';
import { useState } from 'react';
import { useRecoilValue } from 'recoil';

export default function FullSearchBar() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const view = useRecoilValue(viewState);
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const placeholder = 'Search vulnerabilities, oss, or licenses';

  // Wait until detecting appropriate view
  if (view === 'none') return null;

  return (
    <form
      className={clsx(
        'flex gap-x-4 px-4 py-3 bg-semiwhite',
        view === 'pc'
          ? 'rounded shadow-[4px_4px_6px_0_rgba(0,0,0,0.3)] focus-within:shadow-[4px_4px_6px_0_rgb(52,57,63,0.7)]'
          : 'shadow-[0_2px_2px_0_rgba(0,0,0,0.2)] focus-within:shadow-[0_2px_2px_0_rgba(52,57,63,0.7)]'
      )}
      onSubmit={(e) => {
        e.preventDefault();
        router.push(`/search?keyword=${encodeURIComponent(keyword)}`);
      }}
    >
      <input
        className={clsx(
          'flex-1 bg-transparent outline-none font-semibold',
          view === 'pc' && 'text-lg'
        )}
        placeholder={placeholder}
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
      />
      <i className="text-2xl text-darkgray fa-solid fa-magnifying-glass"></i>
    </form>
  );
}
