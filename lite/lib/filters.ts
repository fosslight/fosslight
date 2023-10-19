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

export function parseFilters(str: string) {
  if (!str) {
    return undefined;
  }

  try {
    return JSON.parse(str);
  } catch {
    return undefined;
  }
}
