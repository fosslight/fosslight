'use client';

import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';
import SelfCheckModal from '@/components/self-check-modal';
import { loadingState } from '@/lib/atoms';
import { parseFilters } from '@/lib/filters';
import ExcelIcon from '@/public/images/excel.png';
import dayjs from 'dayjs';
import Image from 'next/image';
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';

export default function SelfCheckList() {
  const setLoading = useSetRecoilState(loadingState);
  const queryParams = useSearchParams();

  // Filters
  const filtersQueryParam = queryParams.get('f') || '';
  const filtersForm = useForm({ defaultValues: parseFilters(filtersQueryParam) });
  const filters: { default: Filter[]; hidden: Filter[] } = {
    default: [
      { label: 'Project ID', name: 'projectId', type: 'number' },
      { label: 'Project Name', name: 'projectName', type: 'char-exact' },
      { label: 'OSS Name', name: 'ossName', type: 'char-exact' },
      { label: 'License Name', name: 'licenseName', type: 'char-exact' },
      { label: 'Created', name: 'created', type: 'date' }
    ],
    hidden: []
  };

  // Modal
  const [isModalShown, setIsModalShown] = useState(false);

  // Rows/Columns
  const [rows, setRows] = useState<any[]>([]);
  const columns = [
    { name: 'ID', sort: 'id' },
    { name: 'Name', sort: 'name' },
    { name: 'Ver', sort: 'ver' },
    { name: 'Report', sort: '' },
    { name: 'Package(s)', sort: '' },
    { name: 'Notice', sort: '' },
    { name: 'Vuln', sort: 'vuln' },
    { name: 'Create', sort: 'create' }
  ];

  // Sorting
  const currentSort = queryParams.get('s') || '';

  // Pagination
  const [totalCount, setTotalCount] = useState(0);
  const currentPage = Number(queryParams.get('p') || '1');
  const countPerPage = 10;

  // Load new rows when changing page or applying filters (including initial load)
  useEffect(() => {
    const params = {
      ...filtersForm.watch(),
      sort: currentSort,
      page: currentPage,
      countPerPage
    };

    setLoading(true);

    setTimeout(() => {
      setTotalCount(24);
      setRows(
        Array.from(Array(params.page < 3 ? 10 : 4)).map((_, idx) => ({
          projectId: String(24 - 10 * (params.page - 1) - idx),
          projectName: 'FOSSLight Hub Lite',
          projectVersion: '1.0.0',
          report: '0',
          packages: ['0', '1'],
          notice: '0',
          cveId: 'CVE-2020-35492',
          cvssScore: '7.8',
          created: '2023-10-05 23:54:08.0'
        }))
      );

      setLoading(false);
    }, 500);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtersQueryParam, currentSort, currentPage, countPerPage]);

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="breadcrumb">Self-Check Project List</h2>

      {/* Description */}
      <h3 className="pb-8">List of projects you&apos;ve created for self-check.</h3>

      {/* Filters */}
      <ListFilters form={filtersForm} filters={filters} />

      {/* Buttons */}
      <div className="flex justify-end gap-x-1 mt-8 mb-4">
        <button className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn">
          <div className="relative w-4 h-4">
            <Image src={ExcelIcon} fill sizes="32px" alt="export" />
          </div>
          Export
        </button>
        <button
          className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn"
          onClick={() => setIsModalShown(true)}
        >
          <i className="text-sm fa-solid fa-plus" /> Create Project
        </button>
      </div>
      <SelfCheckModal
        mode="create"
        data={{ name: '', version: '', comment: '' }}
        show={isModalShown}
        onHide={() => setIsModalShown(false)}
      />

      {/* Table (Rows/Columns + Sorting + Pagination) */}
      <ListTable
        rowId="projectId"
        rows={rows}
        columns={columns}
        currentSort={currentSort}
        pagination={{ totalCount, currentPage, countPerPage }}
        render={(row: any, column: string) => {
          if (column === 'ID') {
            return row.projectId;
          }

          if (column === 'Name') {
            return row.projectName;
          }

          if (column === 'Ver') {
            return row.projectVersion;
          }

          if (column === 'Report') {
            return (
              row.report && (
                <i
                  className="cursor-pointer fa-regular fa-file-excel"
                  title="FOSSLight Report"
                  onClick={(e) => e.stopPropagation()}
                />
              )
            );
          }

          if (column === 'Package(s)') {
            return (
              row.packages.length > 0 && (
                <div className="flex gap-x-2 whitespace-nowrap">
                  {(row.packages as any[]).map((_, idx) => (
                    <i
                      key={idx}
                      className="cursor-pointer fa-solid fa-cube"
                      title={`Package ${idx + 1}`}
                      onClick={(e) => e.stopPropagation()}
                    />
                  ))}
                </div>
              )
            );
          }

          if (column === 'Notice') {
            return (
              row.notice && (
                <i
                  className="cursor-pointer fa-solid fa-file-lines"
                  title="Notice"
                  onClick={(e) => e.stopPropagation()}
                />
              )
            );
          }

          if (column === 'Vuln') {
            return (
              <a
                className="text-orange-500 hover:underline"
                href={`https://nvd.nist.gov/vuln/detail/${row.cveId}`}
                target="_blank"
                onClick={(e) => e.stopPropagation()}
              >
                {row.cvssScore}
              </a>
            );
          }

          if (column === 'Create') {
            return dayjs(row.created.substring(0, 10)).format('YY.MM.DD');
          }

          return null;
        }}
      />
    </>
  );
}
