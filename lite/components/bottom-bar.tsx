import { menus, rootMenu } from '@/lib/literals';

export default function BottomBar() {
  return (
    <div className="fixed right-0 bottom-0 left-0 flex h-16 bg-charcol text-semiwhite">
      <div className="flex-1 pt-2.5 text-center">
        <i className={`text-lg ${rootMenu.icon}`}></i>
        <div className="text-sm">{rootMenu.short}</div>
      </div>
      {menus.map((menu) => (
        <div key={menu.name} className="flex-1 pt-2.5 text-center">
          <i className={`text-lg ${menu.icon}`}></i>
          <div className="text-sm">{menu.short}</div>
        </div>
      ))}
    </div>
  );
}
