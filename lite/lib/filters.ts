import { FieldValues } from 'react-hook-form';

export function stringifyFilters(obj: FieldValues) {
  const cleanedObj = Object.fromEntries(
    Object.entries(obj).filter(
      (p) => p[1] !== '' && p[1] !== false && !(Array.isArray(p[1]) && p[1].length === 0)
    )
  );

  if (Object.keys(cleanedObj).length === 0) {
    return '';
  }

  return JSON.stringify(cleanedObj);
}

export function parseFilters(
  str: string,
  filters: { default: List.Filter[]; hidden: List.Filter[] }
): FieldValues | undefined {
  const defaultValues: any = {};
  filters.default.concat(filters.hidden).forEach((filter) => {
    if (filter.type === 'char-exact') {
      defaultValues[`${filter.name}Exact`] = false;
    } else if (filter.type === 'checkbox') {
      defaultValues[filter.name] = [];
    } else if (filter.type === 'date') {
      defaultValues[`${filter.name}From`] = '';
      defaultValues[`${filter.name}To`] = '';
    } else {
      defaultValues[filter.name] = '';
    }
  });

  if (!str) {
    return defaultValues;
  }

  try {
    return { ...defaultValues, ...JSON.parse(str) };
  } catch {
    return defaultValues;
  }
}
