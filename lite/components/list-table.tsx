import { loadingState, viewState } from '@/lib/atoms';
import { insertCommas } from '@/lib/commons';
import clsx from 'clsx';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useRecoilValue } from 'recoil';

function generatePagination(currPage: number, lastPage: number) {
  const pageCandidates = new Set([
    ...[currPage - 1, currPage, currPage + 1],
    ...[1, 2],
    ...[lastPage - 1, lastPage]
  ]);
  const pages: number[] = [];

  Array.from(pageCandidates)
    .filter((page) => page >= 1 && page <= lastPage)
    .sort()
    .forEach((page, idx, arr) => {
      if (idx > 0 && page - arr[idx - 1] > 1) {
        pages.push(-1);
      }

      pages.push(page);
    });

  return pages;
}

export default function ListTable({
  rows,
  columns,
  currentSort,
  totalCount,
  currentPage,
  countPerPage,
  render
}: {
  rows: any[];
  columns: { name: string; sort: string }[];
  currentSort: string;
  totalCount: number;
  currentPage: number;
  countPerPage: number;
  render: (row: any, column: string) => React.ReactNode;
}) {
  const currentSortObj = Object.fromEntries(currentSort.split(',').map((str) => str.split('-')));
  const lastPage = Math.max(Math.ceil(totalCount / countPerPage), 1);

  const view = useRecoilValue(viewState);
  const loading = useRecoilValue(loadingState);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  function setSort(sort: string) {
    if (!sort || loading) {
      return;
    }

    let newSortList: { key: string; asc: boolean }[];

    if (currentSort) {
      let remove = false;
      let asc = true;

      newSortList = currentSort
        .split(',')
        .map((str) => {
          const [f, d] = str.split('-');
          return { key: f, asc: d === 'asc' };
        })
        .filter((obj) => {
          if (obj.key === sort) {
            if (obj.asc) {
              asc = false;
            } else {
              remove = true;
            }
          }
          return obj.key !== sort;
        });
      if (!remove) {
        newSortList = [{ key: sort, asc }, ...newSortList];
      }
    } else {
      newSortList = [{ key: sort, asc: true }];
    }

    const urlQueryParams = new URLSearchParams(queryParams);

    if (newSortList.length) {
      urlQueryParams.set(
        's',
        newSortList.map((obj) => `${obj.key}-${obj.asc ? 'asc' : 'dsc'}`).join(',')
      );
    } else {
      urlQueryParams.delete('s');
    }

    router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
  }

  function setPage(page: number) {
    if (page === currentPage || loading) {
      return;
    }

    const urlQueryParams = new URLSearchParams(queryParams);

    if (page > 1) {
      urlQueryParams.set('p', String(page));
    } else {
      urlQueryParams.delete('p');
    }

    router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
  }

  return (
    <>
      <div className="relative overflow-x-auto no-scrollbar">
        <table className="w-full min-h-[200px] text-sm">
          {/* Columns */}
          <thead>
            <tr className="border-b-2 border-charcoal/80 text-center whitespace-nowrap">
              {columns.map((column) => (
                <th key={column.name} className="p-2 text-left">
                  <button
                    className="flex gap-x-2"
                    onClick={() => setSort(column.sort)}
                    disabled={!column.sort || loading}
                  >
                    {column.name}

                    {/* Sorting */}
                    {column.sort && (
                      <span className="relative inline-block w-2">
                        {(() => {
                          let [up, down] = [false, false];
                          const d = currentSortObj[column.sort];

                          if (d === 'asc') up = true;
                          if (d === 'dsc') down = true;

                          return (
                            <>
                              <i
                                className={clsx(
                                  'absolute inset-0 pt-1 fa-solid fa-sort-up',
                                  !up && 'text-semigray'
                                )}
                              ></i>
                              <i
                                className={clsx(
                                  'absolute inset-0 pt-1 fa-solid fa-sort-down',
                                  !down && 'text-semigray'
                                )}
                              ></i>
                            </>
                          );
                        })()}
                      </span>
                    )}
                  </button>
                </th>
              ))}
              <th className="w-8 p-2">
                <i className="fa-solid fa-eye"></i>
              </th>
            </tr>
          </thead>

          {/* Rows */}
          <tbody>
            {rows.map((row, idx) => (
              <tr key={idx} className="border-b border-semigray">
                {columns.map((column) => (
                  <td key={column.name} className="px-2 py-1.5">
                    {render(row, column.name)}
                  </td>
                ))}
                <td></td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* When there are no rows */}
        {rows.length === 0 && (
          <div className="absolute top-2/4 right-2/4 translate-x-2/4 -translate-y-2/4 text-center">
            There are no entries.
          </div>
        )}
      </div>

      {/* Pagination */}
      {view !== 'none' && (
        <div
          className={clsx(
            'flex mt-4 items-center',
            view === 'pc' ? 'justify-between' : 'flex-col-reverse gap-y-4'
          )}
        >
          <div className="text-darkgray">
            {rows.length} entries (total {insertCommas(totalCount)} entries)
          </div>
          <div className="flex items-center gap-x-2">
            {generatePagination(currentPage, lastPage).map((page) => {
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
                  onClick={() => setPage(page)}
                  disabled={page === currentPage || loading}
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
