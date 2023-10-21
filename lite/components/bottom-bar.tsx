import { menus, rootMenu } from '@/lib/literals';
import clsx from 'clsx';
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
  }, []);

  return (
    <div className="fixed right-0 bottom-0 left-0 flex h-16 bg-charcoal text-semiwhite">
      <Link
        className={clsx('flex-1 pt-2.5 text-center', pathname === '/' && 'bg-semicharcoal/20')}
        href="/"
      >
        <i className={clsx('text-lg', rootMenu.icon)} />
        <div className="text-sm">{rootMenu.name}</div>
      </Link>
      {menus.map((menu) => {
        if (!menu.sub) {
          return (
            <Link
              key={menu.name}
              className={clsx(
                'flex-1 pt-2.5 text-center no-tap-highlight',
                pathname.startsWith(menu.path) && 'bg-semicharcoal/20'
              )}
              href={menu.path}
            >
              <i className={clsx('text-lg', menu.icon)} />
              <div className="text-sm">{menu.name}</div>
            </Link>
          );
        }

        return (
          <div
            key={menu.name}
            className={clsx(
              'menu relative flex-1 pt-2.5 text-center cursor-pointer no-tap-highlight',
              pathname.startsWith(menu.path) && 'bg-semicharcoal/20'
            )}
            onClick={() =>
              setIsMenuShown({ ...defaultIsMenuShown, [menu.name]: !isMenuShown[menu.name] })
            }
          >
            <i className={clsx('text-lg', menu.icon)} />
            <div className="text-sm">{menu.name}</div>
            <div
              className={clsx(
                'absolute right-0 bottom-16 left-0 flex flex-col bg-charcoal rounded-t-md text-semiwhite overflow-y-hidden transition-[max-height,opacity] duration-[500ms] z-50',
                !isMenuShown[menu.name] ? 'max-h-0 opacity-0' : 'max-h-40 opacity-100'
              )}
            >
              {menu.sub.map((subMenu) => (
                <Link
                  key={subMenu.name}
                  className={clsx(
                    'leading-9',
                    pathname.startsWith(menu.path + subMenu.path) && 'bg-semicharcoal/20'
                  )}
                  href={menu.path + subMenu.path}
                >
                  {subMenu.name}
                </Link>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
