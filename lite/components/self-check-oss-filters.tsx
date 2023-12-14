import clsx from 'clsx';
import { FieldValues, SubmitHandler, UseFormReturn, useFieldArray } from 'react-hook-form';

export default function SelfCheckOSSFilters({
  form,
  onSubmit
}: {
  form: UseFormReturn;
  onSubmit: SubmitHandler<FieldValues>;
}) {
  const { control } = form;
  const sort = useFieldArray({ control, name: 'sort' });
  const sortCriteria = [
    ['oss', 'OSS'],
    ['license', 'License'],
    ['path', 'Path'],
    ['download', 'Download URL'],
    ['homepage', 'Homepage URL']
  ];

  const labelClass = 'flex-shrink-0 w-20 lg:w-24';
  const inputClass = 'px-2 py-1 border border-darkgray outline-none';

  return (
    <form
      className="p-4 mb-4 shadow-[0_1px_4px_0_rgb(34,34,34,0.5)]"
      onSubmit={form.handleSubmit(onSubmit)}
    >
      <div className="grid grid-cols-1 gap-x-8 gap-y-2 text-sm lg:grid-cols-2">
        <div className="flex items-start gap-x-4">
          <div className={labelClass}>OSS/License</div>
          <div className="flex-1">
            <input className={clsx('w-full', inputClass)} {...form.register('keyword')} />
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className={labelClass}>Path</div>
          <div className="flex-1">
            <input className={clsx('w-full', inputClass)} {...form.register('path')} />
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className={labelClass}>Copyright</div>
          <div className="flex-1">
            <input className={clsx('w-full', inputClass)} {...form.register('copyright')} />
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className={labelClass}>URL</div>
          <div className="flex-1">
            <input className={clsx('w-full', inputClass)} {...form.register('url')} />
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className={labelClass}>Sorting</div>
          <div className="flex-1">
            <select
              className={clsx('w-full bg-white', inputClass)}
              onChange={(e) => {
                const key = e.target.value;

                if (!sort.fields.some((field: any) => field.key === key)) {
                  sort.append({ key, asc: true });
                }

                e.target.value = '';
              }}
            >
              <option value="">(Select)</option>
              {sortCriteria.map((option) => (
                <option key={option[0]} value={option[0]}>
                  {option[1]}
                </option>
              ))}
            </select>
            {sort.fields.length > 0 && (
              <div className="flex flex-wrap gap-1 mt-1">
                {sort.fields.map((field: any, idx) => (
                  <div
                    key={field.id}
                    className="px-2 py-0.5 bg-semiwhite border border-darkgray rounded font-semibold cursor-pointer no-tap-highlight"
                    onClick={() => {
                      sort.update(idx, { key: field.key, asc: !field.asc });
                    }}
                  >
                    {Object.fromEntries(sortCriteria)[field.key]}&ensp;
                    {field.asc ? '▲' : '▼'}&ensp;
                    <i
                      className="text-crimson fa-solid fa-x"
                      onClick={(e) => {
                        sort.remove(idx);
                        e.stopPropagation();
                      }}
                    />
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
      <div className="mt-2 text-right">
        <button className="px-2 py-0.5 default-btn">Apply</button>
      </div>
    </form>
  );
}
