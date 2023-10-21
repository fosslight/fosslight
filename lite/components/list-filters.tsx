import { loadingState } from '@/lib/atoms';
import { parseFilters, stringifyFilters } from '@/lib/filters';
import clsx from 'clsx';
import dayjs from 'dayjs';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { FieldValues, UseFormReturn } from 'react-hook-form';
import { useRecoilValue } from 'recoil';

function renderFilters(filters: Filter[], form: UseFormReturn) {
  const labelClass = 'flex-shrink-0 w-20 lg:w-24';
  const inputClass = 'px-2 py-1 border border-darkgray outline-none';

  return filters.map((filter) => {
    // Single-line text
    if (filter.type === 'char' || filter.type === 'char-exact') {
      return (
        <div key={filter.name} className="flex items-start gap-x-4">
          <div className={labelClass}>{filter.label}</div>
          <div className="flex-1">
            <div className="flex gap-x-2">
              <input className={clsx('flex-1 w-0', inputClass)} {...form.register(filter.name)} />
              {filter.type === 'char-exact' && (
                <label className="flex items-center gap-x-1.5">
                  <input
                    className="w-4 h-4"
                    type="checkbox"
                    {...form.register(`${filter.name}Exact`)}
                  />
                  Exact
                </label>
              )}
            </div>
          </div>
        </div>
      );
    }

    // Select
    if (filter.type === 'select') {
      return (
        <div key={filter.name} className="flex items-start gap-x-4">
          <div className={labelClass}>{filter.label}</div>
          <div className="flex-1">
            <select className={clsx('w-full', inputClass)} {...form.register(filter.name)}>
              <option value="">(Select)</option>
              {filter.options?.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      );
    }

    // Checkbox
    if (filter.type === 'checkbox') {
      return (
        <div key={filter.name} className="flex items-start gap-x-4">
          <div className={labelClass}>{filter.label}</div>
          <div className="flex-1">
            <div className="flex flex-wrap gap-x-4 gap-y-1">
              {filter.options?.map((option) => (
                <label key={option.value} className="flex items-center gap-x-1.5">
                  <input
                    className="w-4 h-4"
                    type="checkbox"
                    value={option.value}
                    {...form.register(filter.name)}
                  />
                  {option.label}
                </label>
              ))}
            </div>
          </div>
        </div>
      );
    }

    // Date
    if (filter.type === 'date') {
      return (
        <div key={filter.name} className="flex items-start gap-x-4">
          <div className={labelClass}>{filter.label}</div>
          <div className="flex-1">
            <div className="flex items-center gap-x-2">
              <div className="relative flex-1 w-0">
                <input
                  className={clsx('w-full invisible', inputClass)}
                  type="date"
                  {...form.register(`${filter.name}From`)}
                />
                <div
                  className={clsx('absolute inset-0', inputClass)}
                  onClick={() => {
                    const input = document.querySelector(`[name=${filter.name}From]`);
                    (input as HTMLInputElement).showPicker();
                  }}
                >
                  {(() => {
                    const date = dayjs(form.watch(`${filter.name}From`));
                    if (!date.isValid()) return '';
                    return date.format('YYYY.MM.DD');
                  })()}
                </div>
              </div>
              ~
              <div className="relative flex-1 w-0">
                <input
                  className={clsx('w-full invisible', inputClass)}
                  type="date"
                  {...form.register(`${filter.name}To`)}
                />
                <div
                  className={clsx('absolute inset-0', inputClass)}
                  onClick={() => {
                    const input = document.querySelector(`[name=${filter.name}To]`);
                    (input as HTMLInputElement).showPicker();
                  }}
                >
                  {(() => {
                    const date = dayjs(form.watch(`${filter.name}To`));
                    if (!date.isValid()) return '';
                    return date.format('YYYY.MM.DD');
                  })()}
                </div>
              </div>
            </div>
          </div>
        </div>
      );
    }

    // Number
    if (filter.type === 'number') {
      return (
        <div key={filter.name} className="flex items-start gap-x-4">
          <div className={labelClass}>{filter.label}</div>
          <div className="flex-1">
            <input
              className={clsx('w-full', inputClass)}
              type="number"
              {...form.register(filter.name)}
            />
          </div>
        </div>
      );
    }

    // Multi-line text
    return (
      <div key={filter.name} className="flex items-start gap-x-4">
        <div className={labelClass}>{filter.label}</div>
        <div className="flex-1">
          <textarea
            className={clsx('block w-full resize-none', inputClass)}
            rows={3}
            {...form.register(filter.name)}
          ></textarea>
        </div>
      </div>
    );
  });
}

export default function ListFilters({
  form,
  filters
}: {
  form: UseFormReturn;
  filters: { default: Filter[]; hidden: Filter[] };
}) {
  const { default: defaultFilters, hidden: hiddenFilters } = filters;

  const loading = useRecoilValue(loadingState);
  const [areHiddenFiltersShown, setAreHiddenFiltersShown] = useState(false);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();
  const filtersQueryParam = queryParams.get('f') || '';

  // Reflect states on URL query parameters (state -> URL)
  function setFilters(filterParams: FieldValues) {
    if (loading) {
      return;
    }

    const urlQueryParams = new URLSearchParams(queryParams);

    const f = stringifyFilters(filterParams);
    if (f) {
      urlQueryParams.set('f', f);
    } else {
      urlQueryParams.delete('f');
    }
    urlQueryParams.delete('p');

    router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
  }

  // Reflect URL query parameters on states (URL -> state)
  useEffect(() => {
    const f = stringifyFilters(form.watch());
    if (f !== filtersQueryParam) {
      form.reset(parseFilters(filtersQueryParam), { keepDefaultValues: true });
    }
  }, [filtersQueryParam, form]);

  return (
    <form
      className="relative w-[calc(100%-4px)] p-4 border border-darkgray rounded-lg shadow-[2px_2px_4px_0_rgba(0,0,0,0.2)]"
      onSubmit={form.handleSubmit(setFilters)}
    >
      {/* Default filters */}
      <div className="grid grid-cols-1 gap-x-8 gap-y-2 text-sm lg:grid-cols-2">
        {renderFilters(defaultFilters, form)}
      </div>

      {/* Hidden Filters (If exists) */}
      {hiddenFilters.length > 0 && (
        <>
          <div
            className={clsx(
              'overflow-y-hidden transition-[max-height] duration-500',
              areHiddenFiltersShown ? 'max-h-96' : 'max-h-0'
            )}
          >
            <hr className="my-3 border-semigray" />
            <div className="grid grid-cols-1 gap-x-8 gap-y-2 text-sm lg:grid-cols-2">
              {renderFilters(hiddenFilters, form)}
            </div>
          </div>
          <button
            className="absolute top-full center-x px-2 py-0.5 border-x border-b border-darkgray rounded-b outline-none text-xs"
            type="button"
            onClick={() => setAreHiddenFiltersShown(!areHiddenFiltersShown)}
          >
            {areHiddenFiltersShown ? 'Hide' : 'Expand'}&nbsp;
            {areHiddenFiltersShown ? (
              <i className="fa-solid fa-chevron-up" />
            ) : (
              <i className="fa-solid fa-chevron-down" />
            )}
          </button>
        </>
      )}

      {/* Search button */}
      <div className="mt-2 text-right">
        <button className="px-2 py-1 bg-crimson rounded text-semiwhite" disabled={loading}>
          Search
        </button>
      </div>
    </form>
  );
}
