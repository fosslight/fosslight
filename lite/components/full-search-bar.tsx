export default function FullSearchBar() {
  return (
    <div className="flex gap-x-4 px-4 py-3 bg-semiwhite rounded shadow-[4px_4px_6px_0_rgba(0,0,0,0.3)] focus-within:shadow-[4px_4px_6px_0_rgb(52,57,63,0.8)]">
      <input
        className="flex-1 bg-transparent outline-none text-lg font-semibold"
        placeholder="Search by name of OSS, License, or Project"
      />
      <i className="text-2xl text-darkgray fa-solid fa-magnifying-glass"></i>
    </div>
  );
}
