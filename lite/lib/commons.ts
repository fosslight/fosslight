export function insertCommas(number: number) {
  return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

export function highlight(text: string, keyword?: string) {
  if (!keyword) {
    return text;
  }

  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const highlightedText = text.replace(
    new RegExp(escapedKeyword, 'gi'),
    (match) => `<span class="bg-yellow-200 text-semiblack">${match}</span>`
  );

  return highlightedText;
}
