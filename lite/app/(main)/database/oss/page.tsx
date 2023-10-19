'use client';

import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';
import { loadingState } from '@/lib/atoms';
import { parseFilters } from '@/lib/filters';
import ExcelIcon from '@/public/images/excel.png';
import dayjs from 'dayjs';
import Image from 'next/image';
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';

export default function OSSList() {
  const setLoading = useSetRecoilState(loadingState);
  const queryParams = useSearchParams();

  // Filters
  const filtersQueryParam = queryParams.get('f') || '';
  const filtersForm = useForm({ defaultValues: parseFilters(filtersQueryParam) });
  const filters: { default: Filter[]; hidden: Filter[] } = {
    default: [
      { label: 'OSS Name', name: 'ossName', type: 'char-exact' },
      { label: 'License Name', name: 'licenseName', type: 'char-exact' },
      { label: 'Download URL', name: 'downloadUrl', type: 'char-exact' },
      { label: 'Description', name: 'description', type: 'text' },
      { label: 'Copyright', name: 'copyright', type: 'text' },
      {
        label: 'Deactivate',
        name: 'deactivate',
        type: 'checkbox',
        options: [
          { label: 'YES', value: '0' },
          { label: 'NO', value: '1' }
        ]
      }
    ],
    hidden: [
      {
        label: 'OSS Type',
        name: 'ossType',
        type: 'checkbox',
        options: [
          { label: 'N', value: '0' },
          { label: 'M', value: '1' },
          { label: 'D', value: '2' },
          { label: 'V', value: '3' }
        ]
      },
      {
        label: 'License Type',
        name: 'licenseType',
        type: 'select',
        options: [
          { label: 'Permissive', value: '0' },
          { label: 'Weak Copyleft', value: '1' },
          { label: 'Copyleft', value: '2' },
          { label: 'Proprietary', value: '3' },
          { label: 'Proprietary Free', value: '4' }
        ]
      },
      {
        label: 'Creator',
        name: 'creator',
        type: 'select',
        options: [
          { label: 'CDG', value: '0' },
          { label: 'KSE', value: '1' },
          { label: 'HJH', value: '2' }
        ]
      },
      { label: 'Created', name: 'created', type: 'date' },
      {
        label: 'Modifier',
        name: 'modifier',
        type: 'select',
        options: [
          { label: 'CDG', value: '0' },
          { label: 'KSE', value: '1' },
          { label: 'HJH', value: '2' }
        ]
      },
      { label: 'Modified', name: 'modified', type: 'date' }
    ]
  };

  // Rows/Columns
  const [rows, setRows] = useState<any[]>([]);
  const columns = [
    { name: 'ID', sort: 'id' },
    { name: 'Type', sort: 'type' },
    { name: 'Name', sort: 'name' },
    { name: 'Ver', sort: 'ver' },
    { name: 'License(s)', sort: '' },
    { name: 'Obligation(s)', sort: 'obg' },
    { name: 'URL', sort: '' },
    { name: 'Description', sort: 'desc' },
    { name: 'Vuln', sort: 'vuln' },
    { name: 'Create', sort: 'create' },
    { name: 'Modify', sort: 'modify' }
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
          ossId: String(24 - 10 * (params.page - 1) - idx),
          ossType: 'MD',
          ossName: 'cairo',
          ossVersion: '1.4.12',
          licenseName: '(MPL-1.1 AND GPL-2.0) OR (LGPL-2.1 AND GPL-2.0)',
          licenseType: 'Copyleft',
          obligations: 'YY',
          downloadUrl: 'http://cairographics.org/releases',
          homepageUrl: 'https://www.cairographics.org',
          description: 'Some files in util and test folder are released under GPL-2.0',
          creator: 'admin',
          created: '2023-10-05 23:54:08.0',
          modifier: 'admin',
          modified: '2023-10-07 21:32:05.0',
          cveId: 'CVE-2020-35492',
          cvssScore: '7.8'
        }))
      );

      setLoading(false);
    }, 500);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtersQueryParam, currentSort, currentPage, countPerPage]);

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
      <ListFilters form={filtersForm} filters={filters} />

      {/* Button(s) */}
      <div className="flex justify-end gap-x-2 mt-8 mb-4">
        <button className="flex items-center gap-x-1.5 px-2 py-0.5 border border-gray rounded">
          <div className="relative w-4 h-4">
            <Image src={ExcelIcon} fill sizes="32px" alt="export" />
          </div>
          Export
        </button>
      </div>

      {/* Table (Rows/Columns + Sorting + Pagination) */}
      <ListTable
        rows={rows}
        columns={columns}
        currentSort={currentSort}
        totalCount={totalCount}
        currentPage={currentPage}
        countPerPage={countPerPage}
        render={(row: any, column: string) => {
          if (column === 'ID') {
            return row.ossId;
          }

          if (column === 'Type') {
            return <div className="whitespace-nowrap">{row.ossType.split('').join(', ')}</div>;
          }

          if (column === 'Name') {
            return row.ossName;
          }

          if (column === 'Ver') {
            return row.ossVersion;
          }

          if (column === 'License(s)') {
            return (
              <>
                [{row.licenseType}]<br />
                {row.licenseName}
              </>
            );
          }

          if (column === 'Obligation(s)') {
            const notice = row.obligations[0] === 'Y';
            const source = row.obligations[1] === 'Y';

            return (
              <div className="flex gap-x-2 whitespace-nowrap">
                {notice && <i className="fa-solid fa-file-lines" title="Notice"></i>}
                {source && <i className="fa-solid fa-code" title="Source"></i>}
              </div>
            );
          }

          if (column === 'URL') {
            return (
              <div className="whitespace-nowrap">
                <a className="text-blue-500 hover:underline" href={row.downloadUrl} target="_blank">
                  Download
                </a>
                <br />
                <a className="text-blue-500 hover:underline" href={row.homepageUrl} target="_blank">
                  Homepage
                </a>
              </div>
            );
          }

          if (column === 'Description') {
            return <div className="whitespace-pre-line">{row.description}</div>;
          }

          if (column === 'Vuln') {
            return (
              <a
                className="text-orange-500 hover:underline"
                href={`https://nvd.nist.gov/vuln/detail/${row.cveId}`}
                target="_blank"
              >
                {row.cvssScore}
              </a>
            );
          }

          if (column === 'Create') {
            return (
              <div className="whitespace-nowrap">
                {dayjs(row.created.substring(0, 10)).format('YY.MM.DD')}
                <br />({row.creator})
              </div>
            );
          }

          if (column === 'Modify') {
            return (
              <div className="whitespace-nowrap">
                {dayjs(row.modified.substring(0, 10)).format('YY.MM.DD')}
                <br />({row.modifier})
              </div>
            );
          }

          return null;
        }}
      />
    </>
  );
}
