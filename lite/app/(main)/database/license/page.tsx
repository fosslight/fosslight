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

export default function LicenseList() {
  const setLoading = useSetRecoilState(loadingState);
  const queryParams = useSearchParams();

  // Filters
  const filtersQueryParam = queryParams.get('f') || '';
  const filtersForm = useForm({ defaultValues: parseFilters(filtersQueryParam) });
  const filters: { default: Filter[]; hidden: Filter[] } = {
    default: [
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
      { label: 'License Name', name: 'licenseName', type: 'char-exact' },
      {
        label: 'Restriction(s)',
        name: 'restrictions',
        type: 'checkbox',
        options: [
          { label: 'Non-commercial Use Only', value: '0' },
          { label: 'Network Copyleft', value: '1' },
          { label: 'Restricted Modifications', value: '2' },
          { label: 'Platform Deployment Restriction', value: '3' },
          { label: 'Prohibited Purpose', value: '4' },
          { label: 'Specification Constraints', value: '5' },
          { label: 'Restricted Redistribution', value: '6' },
          { label: 'Commons Clause Restriction', value: '7' }
        ]
      },
      {
        label: 'Obligation(s)',
        name: 'obligations',
        type: 'checkbox',
        options: [
          { label: 'Notice', value: '0' },
          { label: 'Source', value: '1' }
        ]
      },
      { label: 'Homepage URL', name: 'homepageUrl', type: 'char-exact' },
      { label: 'Description', name: 'description', type: 'text' },
      { label: 'License Text', name: 'licenseText', type: 'text' }
    ],
    hidden: [
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
    { name: 'Identifier', sort: 'idf' },
    { name: 'Restriction(s)', sort: 'res' },
    { name: 'Obligation(s)', sort: 'obg' },
    { name: 'URL', sort: 'url' },
    { name: 'Description', sort: 'desc' },
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
          licenseId: String(24 - 10 * (params.page - 1) - idx),
          licenseType: 'Permissive',
          licenseName: 'Apache License 2.0',
          licenseIdentifier: 'Apache-2.0',
          restrictions: ['Non-commercial Use Only', 'Network Copyleft'],
          obligations: 'YY',
          homepageUrl: 'https://spdx.org/licenses/blessing.html',
          description: 'There are some descriptions here.',
          creator: 'admin',
          created: '2023-10-05 23:54:08.0',
          modifier: 'admin',
          modified: '2023-10-07 21:32:05.0'
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
        <i className="mx-2 text-sm fa-solid fa-angle-right" />
        License List
      </h2>

      {/* Description */}
      <h3 className="pb-8">List of license information registered in the database.</h3>

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
        rowId="licenseId"
        rows={rows}
        columns={columns}
        currentSort={currentSort}
        totalCount={totalCount}
        currentPage={currentPage}
        countPerPage={countPerPage}
        render={(row: any, column: string) => {
          if (column === 'ID') {
            return row.licenseId;
          }

          if (column === 'Type') {
            return row.licenseType;
          }

          if (column === 'Name') {
            return row.licenseName;
          }

          if (column === 'Identifier') {
            return row.licenseIdentifier;
          }

          if (column === 'Restriction(s)') {
            return <div className="whitespace-pre">{row.restrictions.join('\n')}</div>;
          }

          if (column === 'Obligation(s)') {
            const notice = row.obligations[0] === 'Y';
            const source = row.obligations[1] === 'Y';

            return (
              <div className="flex gap-x-2 whitespace-nowrap">
                {notice && <i className="fa-solid fa-file-lines" title="Notice" />}
                {source && <i className="fa-solid fa-code" title="Source" />}
              </div>
            );
          }

          if (column === 'URL') {
            return (
              <a
                className="text-blue-500 whitespace-nowrap hover:underline"
                href={row.homepageUrl}
                target="_blank"
                onClick={(e) => e.stopPropagation()}
              >
                Homepage
              </a>
            );
          }

          if (column === 'Description') {
            return <div className="whitespace-pre-line">{row.description}</div>;
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
