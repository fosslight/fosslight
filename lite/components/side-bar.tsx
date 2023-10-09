'use client';

import { menus, rootMenu } from '@/lib/literals';
import Logo from '@/public/images/logo.png';
import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function SideBar({ isShown }: { isShown: boolean }) {
  const [isMenuShown, setIsMenuShown] = useState(
    Object.fromEntries(menus.map((menu) => [menu.name, true]))
  );
  const pathname = usePathname();

  useEffect(() => {
    const currentMenu = menus.filter((menu) => pathname.startsWith(menu.path))[0];

    if (currentMenu && !isMenuShown[currentMenu.name]) {
      const newIsMenuShown = { ...isMenuShown };
      newIsMenuShown[currentMenu.name] = true;
      setIsMenuShown(newIsMenuShown);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname]);

  return (
    <div
      className={`sticky top-0 ${
        isShown ? 'w-56' : 'w-0'
      } shrink-0 h-screen bg-charcol shadow-[0_0_6px_2px_rgba(0,0,0,0.5)] text-semiwhite transition-[width] duration-300`}
    >
      <div className="absolute top-0 right-0 bottom-0 w-56 overflow-y-auto no-scrollbar">
        <div className="sticky top-0 bg-charcol">
          <div className="flex justify-center items-center gap-x-4 py-4">
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
          </div>
          <div className="flex items-center gap-x-4 px-4 py-3 shadow-[0_0_6px_2px_rgba(0,0,0,0.5)]">
            <i className="text-lg fa-solid fa-user"></i>
            <div className="flex-1 text-xs overflow-x-hidden">
              최덕경
              <br />
              <div className="leading-none overflow-hidden text-ellipsis">hjcdg197@gmail.com</div>
            </div>
          </div>
        </div>
        <div className="flex flex-col gap-y-4 py-8">
          <Link
            className={`px-4 text-lg font-semibold leading-loose ${
              pathname === '/' ? 'bg-[rgb(104,114,126,0.2)]' : ''
            }`}
            href="/"
          >
            <i className={rootMenu.icon}></i>&ensp;{rootMenu.name}
          </Link>
          {menus.map((menu) => {
            return (
              <div key={menu.name}>
                <div
                  className="flex justify-between items-center px-4 cursor-pointer"
                  onClick={() => {
                    const newIsMenuShown = { ...isMenuShown };
                    newIsMenuShown[menu.name] = !newIsMenuShown[menu.name];
                    setIsMenuShown(newIsMenuShown);
                  }}
                >
                  <div className="text-lg font-semibold leading-loose">
                    <i className={menu.icon}></i>&ensp;{menu.name}
                  </div>
                  {isMenuShown[menu.name] ? (
                    <i className="fa-solid fa-chevron-down"></i>
                  ) : (
                    <i className="fa-solid fa-chevron-up"></i>
                  )}
                </div>
                <div
                  className={`flex flex-col ${
                    !isMenuShown[menu.name] ? 'max-h-0' : 'max-h-40'
                  } overflow-y-hidden transition-[max-height] duration-300`}
                >
                  {menu.sub.map((subMenu) => (
                    <Link
                      key={subMenu.name}
                      className={`px-4 ${
                        pathname.startsWith(menu.path + subMenu.path)
                          ? 'bg-[rgb(127,141,157,0.2)]'
                          : ''
                      } leading-loose cursor-pointer hover:bg-[rgb(104,114,126,0.5)]`}
                      href={menu.path + subMenu.path}
                    >
                      <i className="fa-solid fa-caret-right"></i>&emsp;{subMenu.name}
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
