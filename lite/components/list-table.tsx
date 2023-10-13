import { viewState } from '@/lib/atoms';
import { insertCommas } from '@/lib/commons';
import clsx from 'clsx';
import { useRecoilValue } from 'recoil';

export default function ListTable() {
  const view = useRecoilValue(viewState);
  const countPerPage = 10;
  const totalCount = 30924;
  const pages = [1, 2, 3, -1, 7, 8, 9];
  const currentPage = 2;

  return (
    <>
      <div className="overflow-x-auto no-scrollbar">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b-2 border-charcoal/80 text-center whitespace-nowrap">
              <th className="p-2 text-left">
                ID<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                Type<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                Name<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                Ver<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                License(s)<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                Obligation(s)<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                Download<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="p-2 text-left">
                Vuln<i className="ml-2 text-semigray fa-solid fa-sort"></i>
              </th>
              <th className="w-8 p-2">
                <i className="fa-solid fa-eye"></i>
              </th>
            </tr>
          </thead>
          <tbody>
            {Array.from(Array(10)).map((_, idx) => (
              <tr key={idx} className="border-b border-semigray">
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td className="px-2 py-1.5">Data</td>
                <td></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {view !== 'none' && (
        <div
          className={clsx(
            'flex mt-4 items-center',
            view === 'pc' ? 'justify-between' : 'flex-col-reverse gap-y-4'
          )}
        >
          <div className="text-darkgray">
            {countPerPage} entries (total {insertCommas(totalCount)} entries)
          </div>
          <div className="flex items-center gap-x-2">
            {pages.map((page) => {
              if (page === -1) {
                return <i key={page} className="fa-solid fa-ellipsis"></i>;
              }

              return (
                <button
                  key={page}
                  className={clsx(
                    'px-2 py-0.5 border',
                    page === currentPage
                      ? 'bg-charcoal border-charcoal text-semiwhite'
                      : 'border-darkgray'
                  )}
                >
                  {page}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </>
  );
}
