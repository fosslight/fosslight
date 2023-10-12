import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';
import ExcelIcon from '@/public/images/excel.png';
import Image from 'next/image';

export default function OSSList() {
  return (
    <>
      {/* Breadcrumb */}
      <h2 className="pb-2 text-xl font-black">
        Database
        <i className="mx-2 text-sm fa-solid fa-angle-right"></i>
        OSS List
      </h2>

      {/* Description */}
      <h3 className="pb-8">
        List of OSS(= Open Source Software) information registered in the database.
      </h3>

      {/* Filters */}
      <ListFilters />

      {/* Button(s) */}
      <div className="flex justify-end gap-x-2 mt-8">
        <button className="flex items-center gap-x-1.5 px-2 py-0.5 border border-gray rounded">
          <div className="relative w-4 h-4">
            <Image src={ExcelIcon} fill sizes="32px" alt="export" />
          </div>
          Export
        </button>
      </div>

      {/* Table */}
      <ListTable />
    </>
  );
}
