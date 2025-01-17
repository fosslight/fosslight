import { userState } from '@/lib/atoms';
import { MENUS, ROOT_MENU } from '@/lib/literals';
import Logo from '@/public/images/logo.png';
import clsx from 'clsx';
import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useRecoilValue } from 'recoil';

export default function SideBar({ isShown }: { isShown: boolean }) {
  const user = useRecoilValue(userState);
  const [isMenuShown, setIsMenuShown] = useState(
    Object.fromEntries(MENUS.map((menu) => [menu.name, true]))
  );
  const pathname = usePathname();

  useEffect(() => {
    const currentMenu = MENUS.filter((menu) => pathname.startsWith(menu.path))[0];

    if (currentMenu && currentMenu.sub && !isMenuShown[currentMenu.name]) {
      setIsMenuShown({ ...isMenuShown, [currentMenu.name]: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);

  return (
    <div
      className={clsx(
        'sticky top-0 shrink-0 h-screen bg-charcoal shadow-[0_0_6px_2px_rgba(0,0,0,0.5)] text-semiwhite transition-[width] duration-300 z-20',
        isShown ? 'w-56' : 'w-0'
      )}
    >
      <div className="absolute top-0 right-0 bottom-0 w-56 overflow-y-auto no-scrollbar">
        <div className="sticky top-0 bg-charcoal">
          <Link className="flex justify-center items-center gap-x-4 py-4" href="/">
            <div className="w-12 h-12 p-3 bg-white rounded-full">
              <div className="relative w-6 h-6">
                <Image src={Logo} fill sizes="48px" alt="fosslight" />
              </div>
            </div>
            <div className="font-semibold">
              FOSSLight Hub
              <br />
              <span className="font-light">Lite</span>
            </div>
          </Link>
          <div className="flex items-center gap-x-4 px-4 py-3 shadow-[0_0_6px_2px_rgba(0,0,0,0.5)]">
            <i className="text-lg fa-solid fa-user" />
            <div className="flex-1 text-xs overflow-x-hidden">
              {user ? (
                <div className="leading-tight">
                  {user.name}
                  <br />
                  <div className="overflow-hidden text-ellipsis">{user.email}</div>
                </div>
              ) : (
                <span className="leading-tight opacity-70">
                  Loading
                  <br />
                  My Information...
                </span>
              )}
            </div>
          </div>
        </div>
        <div className="flex flex-col gap-y-4 py-8">
          <Link
            className={clsx(
              'px-4 text-lg font-semibold leading-loose hover:bg-semicharcoal/50',
              pathname === '/' && 'bg-semicharcoal/20'
            )}
            href="/"
          >
            <i className={ROOT_MENU.icon} />
            &ensp;{ROOT_MENU.name}
          </Link>
          {MENUS.map((menu) => {
            if (!menu.sub) {
              return (
                <Link
                  key={menu.name}
                  className={clsx(
                    'px-4 text-lg font-semibold leading-loose hover:bg-semicharcoal/50',
                    pathname.startsWith(menu.path) && 'bg-semicharcoal/20'
                  )}
                  href={menu.path}
                >
                  <i className={menu.icon} />
                  &ensp;{menu.name}
                </Link>
              );
            }

            return (
              <div key={menu.name}>
                <div
                  className="flex justify-between items-center px-4 cursor-pointer"
                  onClick={() =>
                    setIsMenuShown({ ...isMenuShown, [menu.name]: !isMenuShown[menu.name] })
                  }
                >
                  <div className="text-lg font-semibold leading-loose">
                    <i className={menu.icon} />
                    &ensp;{menu.name}
                  </div>
                  {isMenuShown[menu.name] ? (
                    <i className="fa-solid fa-chevron-down" />
                  ) : (
                    <i className="fa-solid fa-chevron-up" />
                  )}
                </div>
                <div
                  className={clsx(
                    'flex flex-col overflow-y-hidden transition-[max-height] duration-[500ms]',
                    !isMenuShown[menu.name] ? 'max-h-0' : 'max-h-40'
                  )}
                >
                  {menu.sub.map((subMenu) => (
                    <Link
                      key={subMenu.name}
                      className={clsx(
                        'px-4 leading-loose cursor-pointer hover:bg-semicharcoal/50',
                        pathname.startsWith(menu.path + subMenu.path) && 'bg-semicharcoal/20'
                      )}
                      href={menu.path + subMenu.path}
                    >
                      <i className="fa-solid fa-caret-right" />
                      &emsp;{subMenu.name}
                    </Link>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
