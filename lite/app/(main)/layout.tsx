'use client';

import BottomBar from '@/components/bottom-bar';
import DetailModal from '@/components/detail-modal';
import FullSearchBar from '@/components/full-search-bar';
import Loading from '@/components/loading';
import SideBar from '@/components/side-bar';
import TopBar from '@/components/top-bar';
import { loadingState, userState, viewState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import clsx from 'clsx';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useMediaQuery } from 'react-responsive';
import { useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';

export default function Layout({ children }: { children: React.ReactNode }) {
  const setUser = useSetRecoilState(userState);
  const [view, setView] = useRecoilState(viewState);
  const loading = useRecoilValue(loadingState);
  const [isSideBarShown, setIsSideBarShown] = useState(true);
  const router = useRouter();
  const isMobile = useMediaQuery({ maxWidth: 768 });
  const isSubwindow = Boolean(window.opener);

  // API for loading my info
  const loadMeRequest = useAPI('get', 'http://localhost:8180/api/lite/me', {
    onSuccess: (res) => {
      setUser({ name: res.data.username, email: res.data.email });
    },
    onError: () => {
      router.push('/sign-in');
    }
  });

  useEffect(() => {
    loadMeRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (isMobile) setView('mobile');
    else setView('pc');
  }, [setView, isMobile]);

  // Wait until detecting appropriate view
  if (view === 'none') return null;

  return (
    <main className={clsx('min-h-screen', view === 'pc' && 'flex')}>
      {/* Left navigation bar (PC) */}
      {view === 'pc' && !isSubwindow && <SideBar isShown={isSideBarShown} />}

      {/* Page body */}
      <div className={view === 'pc' ? 'flex-1 min-w-0' : ''}>
        {/* Areas fixed at the top of the page */}
        {!isSubwindow && (
          <div
            className={clsx(
              'sticky top-0 bg-white z-10',
              view === 'pc' && 'flex flex-col gap-y-10 pt-4 px-4 pb-4'
            )}
          >
            {/* Hamburger button (PC) or Top bar (Mobile) */}
            {view === 'pc' ? (
              <button
                className="w-6 h-6 text-xl text-charcoal"
                onClick={() => setIsSideBarShown(!isSideBarShown)}
              >
                <i className="fa-solid fa-bars" />
              </button>
            ) : (
              <TopBar />
            )}

            {/* Full search bar */}
            <FullSearchBar />
          </div>
        )}

        {/* Page content */}
        <div
          className={clsx(
            'pt-4 mx-4 overflow-x-auto transition-opacity duration-300 no-scrollbar',
            view === 'pc' ? 'pb-8' : 'pt-4 pb-24',
            loading && 'opacity-30'
          )}
        >
          {children}

          {/* Loading */}
          {loading && (
            <div className="fixed center">
              <Loading />
            </div>
          )}
        </div>
      </div>

      {/* Bottom navigation bar (Mobile) */}
      {view === 'mobile' && !isSubwindow && <BottomBar />}

      {/* Modal for detail view */}
      <DetailModal />
    </main>
  );
}
