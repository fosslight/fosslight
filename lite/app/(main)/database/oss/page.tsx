import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';

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

      {/* Table */}
      <ListTable />
    </>
  );
}
