import Logo from '@/public/images/logo.png';
import Image from 'next/image';

export default function SideBar({ isShown }: { isShown: boolean }) {
  return (
    <div
      className={`sticky top-0 ${
        isShown ? 'w-56' : 'w-0'
      } h-screen bg-charcol shadow-[0_0_6px_2px_rgba(0,0,0,0.5)] text-semiwhite transition-[width] duration-300`}
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
      </div>
    </div>
  );
}
