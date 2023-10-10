'use client';

import { menus, rootMenu } from '@/lib/literals';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';

const defaultIsMenuShown = Object.fromEntries(menus.map((menu) => [menu.name, false]));

export default function BottomBar() {
  const [isMenuShown, setIsMenuShown] = useState(defaultIsMenuShown);
  const pathname = usePathname();

  useEffect(() => {
    function handleClickOutsideSubMenu(e: MouseEvent) {
      if (e.target && !(e.target as Element).closest('.menu')) {
        setIsMenuShown({ ...defaultIsMenuShown });
      }
    }

    document.addEventListener('mousedown', handleClickOutsideSubMenu);

    return () => {
      document.removeEventListener('mousedown', handleClickOutsideSubMenu);
    };
  });

  return (
    <div className="fixed right-0 bottom-0 left-0 flex h-16 bg-charcol text-semiwhite">
      <Link
        className={`flex-1 pt-2.5 text-center ${pathname === '/' ? 'bg-semicharcol/20' : ''}`}
        href="/"
      >
        <i className={`text-lg ${rootMenu.icon}`}></i>
        <div className="text-sm">{rootMenu.short}</div>
      </Link>
      {menus.map((menu) => {
        if (!menu.sub) {
          return (
            <Link
              key={menu.name}
              className={`flex-1 pt-2.5 text-center ${
                pathname.startsWith(menu.path) ? 'bg-semicharcol/20' : ''
              }`}
              href={menu.path}
            >
              <i className={`text-lg ${menu.icon}`}></i>
              <div className="text-sm">{menu.short}</div>
            </Link>
          );
        }

        return (
          <div
            key={menu.name}
            className={`menu relative flex-1 pt-2.5 text-center cursor-pointer no-tap-highlight ${
              pathname.startsWith(menu.path) ? 'bg-semicharcol/20' : ''
            }`}
            onClick={() => {
              const newIsMenuShown = {
                ...defaultIsMenuShown,
                [menu.name]: !isMenuShown[menu.name]
              };
              setIsMenuShown(newIsMenuShown);
            }}
          >
            <i className={`text-lg ${menu.icon}`}></i>
            <div className="text-sm">{menu.short}</div>
            <div
              className={`absolute right-0 bottom-16 left-0 flex flex-col bg-charcol border-b border-darkgray rounded-t-md text-semiwhite overflow-y-hidden transition-[max-height,opacity] duration-[500ms] z-50 ${
                !isMenuShown[menu.name] ? 'max-h-0 opacity-0' : 'max-h-40 opacity-100'
              }`}
            >
              {menu.sub.map((subMenu) => (
                <Link
                  key={subMenu.name}
                  className={`leading-9 ${
                    pathname.startsWith(menu.path + subMenu.path) ? 'bg-semicharcol/20' : ''
                  }`}
                  href={menu.path + subMenu.path}
                >
                  {subMenu.short}
                </Link>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
