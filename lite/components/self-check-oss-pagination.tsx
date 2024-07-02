import clsx from 'clsx';
import { useEffect, useState } from 'react';

export default function SelfCheckOSSPagination({
  currentPage,
  setCurrentPage,
  lastPage
}: {
  currentPage: number;
  setCurrentPage: (currentPage: number) => void;
  lastPage: number;
}) {
  const [pageDisplay, setPageDisplay] = useState(String(currentPage));

  useEffect(() => {
    setPageDisplay(String(currentPage));
  }, [currentPage]);

  return (
    <div className="flex justify-center items-center gap-x-3 mt-4">
      <i
        className={clsx(
          'fa-solid fa-angles-left',
          currentPage > 1 ? 'text-charcoal cursor-pointer no-tap-highlight' : 'text-semigray'
        )}
        onClick={() => currentPage > 1 && setCurrentPage(1)}
      />
      <i
        className={clsx(
          'fa-solid fa-angle-left',
          currentPage > 1 ? 'text-charcoal cursor-pointer no-tap-highlight' : 'text-semigray'
        )}
        onClick={() => currentPage > 1 && setCurrentPage(currentPage - 1)}
      />
      <div className="flex justify-center items-center">
        Page&ensp;
        <input
          className="w-10 px-1 border border-darkgray outline-none text-center"
          value={pageDisplay}
          onChange={(e) => setPageDisplay(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              const input = Number(pageDisplay);

              let newPage: number;
              if (Number.isNaN(input) || input < 1) {
                newPage = 1;
              } else if (input > lastPage) {
                newPage = lastPage;
              } else {
                newPage = input;
              }

              setCurrentPage(newPage);
            }
          }}
          onBlur={() => setPageDisplay(String(currentPage))}
        />
        &nbsp; of {lastPage}
      </div>
      <i
        className={clsx(
          'fa-solid fa-angle-right',
          currentPage < lastPage ? 'text-charcoal cursor-pointer no-tap-highlight' : 'text-semigray'
        )}
        onClick={() => currentPage < lastPage && setCurrentPage(currentPage + 1)}
      />
      <i
        className={clsx(
          'fa-solid fa-angles-right',
          currentPage < lastPage ? 'text-charcoal cursor-pointer no-tap-highlight' : 'text-semigray'
        )}
        onClick={() => currentPage < lastPage && setCurrentPage(lastPage)}
      />
    </div>
  );
}
