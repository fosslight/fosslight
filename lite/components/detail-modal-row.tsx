export default function DetailModalRow({
  label,
  value,
  bottomBorder = false
}: {
  label: React.ReactNode;
  value: React.ReactNode;
  bottomBorder?: boolean;
}) {
  return (
    <>
      <div className="flex flex-col gap-y-3 lg:flex-row lg:gap-x-2">
        <div className="flex-shrink-0 w-full font-bold lg:w-36">{label}</div>
        <div className="flex-1 break-all">{value}</div>
      </div>
      {bottomBorder && <hr className="border-semiwhite" />}
    </>
  );
}
