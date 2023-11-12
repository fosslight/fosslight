import { loadingState, viewState } from '@/lib/atoms';
import { insertCommas } from '@/lib/commons';
import clsx from 'clsx';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
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
  pagination,
  render,
  onClickRow
}: {
  rows: any[];
  columns: List.Column[];
  currentSort?: string;
  pagination?: { totalCount: number; currentPage: number; countPerPage: number };
  render: (row: any, column: string) => React.ReactNode;
  onClickRow: (row: any) => void;
}) {
  const currentSortObj = Object.fromEntries(
    (currentSort || '').split(',').map((str) => str.split('-'))
  );

  const view = useRecoilValue(viewState);
  const loading = useRecoilValue(loadingState);
  const [isColumnSelectorShown, setIsColumnSelectorShown] = useState(false);
  const [isColumnShown, setIsColumnShown] = useState(
    Object.fromEntries(columns.map((column) => [column.name, true]))
  );

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
    if (!pagination || page === pagination.currentPage || loading) {
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

  useEffect(() => {
    function handleClickOutsideColumnSelector(e: MouseEvent) {
      if (e.target && !(e.target as Element).closest('.column-selector')) {
        setIsColumnSelectorShown(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutsideColumnSelector);
    return () => {
      document.removeEventListener('mousedown', handleClickOutsideColumnSelector);
    };
  }, []);

  return (
    <>
      <div className="relative overflow-x-auto no-scrollbar">
        <table className={clsx('w-full text-sm', rows.length === 0 && 'min-h-[200px]')}>
          {/* Columns */}
          <thead>
            <tr className="border-b-2 border-charcoal/80 text-left whitespace-nowrap">
              {columns
                .filter((column) => isColumnShown[column.name])
                .map((column) => (
                  <th key={column.name} className="p-2">
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
                                />
                                <i
                                  className={clsx(
                                    'absolute inset-0 pt-1 fa-solid fa-sort-down',
                                    !down && 'text-semigray'
                                  )}
                                />
                              </>
                            );
                          })()}
                        </span>
                      )}
                    </button>
                  </th>
                ))}
              <th className="column-selector relative w-8 p-2">
                <button onClick={() => setIsColumnSelectorShown(!isColumnSelectorShown)}>
                  <i className="fa-solid fa-eye" />
                </button>
                {isColumnSelectorShown && (
                  <div className="absolute top-full right-0 flex flex-col gap-y-1.5 p-3 mt-0.5 bg-white border-x border-b border-darkgray rounded-b shadow-[-2px_2px_4px_0_rgba(0,0,0,0.2)]">
                    {columns.map((column) => (
                      <label key={column.name} className="flex justify-end items-center gap-x-2">
                        {column.name}
                        <input
                          type="checkbox"
                          checked={isColumnShown[column.name]}
                          onChange={(e) => {
                            const { checked } = e.target;
                            const checkedCnt = Object.values(isColumnShown).filter((isShown) =>
                              Boolean(isShown)
                            ).length;

                            if (!checked && checkedCnt <= 3) {
                              return;
                            }

                            setIsColumnShown({
                              ...isColumnShown,
                              [column.name]: checked
                            });
                          }}
                        />
                      </label>
                    ))}
                  </div>
                )}
              </th>
            </tr>
          </thead>

          {/* Rows */}
          <tbody>
            {rows.map((row, idx) => (
              <tr
                key={idx}
                className="border-b border-semigray cursor-pointer hover:opacity-80"
                onClick={() => onClickRow(row)}
              >
                {columns
                  .filter((column) => isColumnShown[column.name])
                  .map((column) => (
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
        {rows.length === 0 && <div className="absolute center">There are no entries.</div>}
      </div>

      {/* Pagination */}
      {view !== 'none' && pagination && (
        <div
          className={clsx(
            'flex mt-4 items-center',
            view === 'pc' ? 'justify-between' : 'flex-col-reverse gap-y-4'
          )}
        >
          <div className="text-darkgray">
            {rows.length} entries (total {insertCommas(pagination.totalCount)} entries)
          </div>
          <div className="flex items-center gap-x-2">
            {generatePagination(
              pagination.currentPage,
              Math.max(Math.ceil(pagination.totalCount / pagination.countPerPage), 1)
            ).map((page) => {
              if (page === -1) {
                return <i key={page} className="fa-solid fa-ellipsis" />;
              }

              return (
                <button
                  key={page}
                  className={clsx(
                    'px-2 py-0.5 border',
                    page === pagination.currentPage
                      ? 'bg-charcoal border-charcoal text-semiwhite'
                      : 'border-darkgray'
                  )}
                  onClick={() => setPage(page)}
                  disabled={page === pagination.currentPage || loading}
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
