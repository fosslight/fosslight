import clsx from 'clsx';

export default function Dashboard() {
  const sectionHeaderClass = 'pl-2 mb-4 border-l-4 border-l-semiblack font-bold leading-tight';
  const sectionClass =
    'p-4 border border-darkgray rounded-lg shadow-[2px_2px_4px_0_rgba(0,0,0,0.2)]';

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="pb-2 text-xl font-black">Dashboard</h2>

      {/* Description */}
      <h3 className="pb-8">
        Insights on your projects, and recently registered OSSs, licenses, vulnerabilities.
      </h3>

      {/* Recent Projects */}
      <h4 className={sectionHeaderClass}>Insights on Projects</h4>
      <div className={clsx('w-[calc(100%-4px)] mb-8', sectionClass)}>...</div>

      {/* Newly Registered in Database */}
      <h4 className={sectionHeaderClass}>Recently Registered in Database</h4>
      <div className="grid grid-cols-2 gap-4 w-[calc(100%-4px)]">
        <div className={clsx('col-span-2', sectionClass)}>...</div>
        <div className={clsx('col-span-2 lg:col-span-1', sectionClass)}>...</div>
        <div className={clsx('col-span-2 lg:col-span-1', sectionClass)}>...</div>
      </div>
    </>
  );
}
