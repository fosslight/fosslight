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
import { useEffect, useRef, useState } from 'react';
import { useMediaQuery } from 'react-responsive';
import { useRecoilState, useRecoilValue } from 'recoil';

export default function Layout({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useRecoilState(userState);
  const [view, setView] = useRecoilState(viewState);
  const loading = useRecoilValue(loadingState);
  const [isSideBarShown, setIsSideBarShown] = useState(true);
  const userLoadingRef = useRef<HTMLDivElement>(null);
  const router = useRouter();
  const isMobile = useMediaQuery({ maxWidth: 768 });
  const isSubwindow = typeof window !== 'undefined' && Boolean(window.opener);

  // API for loading my info
  const loadMeRequest = useAPI('get', '/api/lite/me', {
    onSuccess: (res) => {
      setUser({ name: res.data.username, email: res.data.email });

      const userLoading = userLoadingRef.current;
      userLoading?.addEventListener('transitionend', function handleTransitionEnd() {
        userLoading?.removeEventListener('transitionend', handleTransitionEnd);
        userLoading.style.display = 'none';
      });
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
    <>
      {/* Wait until detecting logined user */}
      <div
        className={clsx(
          'fixed center flex flex-col items-center z-[1000] transition-opacity duration-200',
          user ? 'opacity-0' : 'opacity-100'
        )}
        ref={userLoadingRef}
      >
        <div className="mb-4 text-lg font-bold">Loading . . .</div>
        <Loading />
      </div>

      {/* Main content */}
      <main
        className={clsx(
          'min-h-screen transition-opacity duration-200',
          view === 'pc' && 'flex',
          user ? 'opacity-100' : 'opacity-0'
        )}
      >
        {/* Left navigation bar (PC) */}
        {view === 'pc' && !isSubwindow && <SideBar isShown={isSideBarShown} />}

        {/* Page body */}
        <div className={clsx(view === 'pc' && 'flex-1 min-w-0', loading && 'opacity-50')}>
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
              view === 'pc' ? 'pb-8' : 'pt-4 pb-24'
            )}
          >
            {children}
          </div>
        </div>

        {/* Bottom navigation bar (Mobile) */}
        {view === 'mobile' && !isSubwindow && <BottomBar />}

        {/* Modal for detail view */}
        <DetailModal />

        {/* Loading */}
        {loading && (
          <div className="fixed center">
            <Loading />
          </div>
        )}
      </main>
    </>
  );
}
