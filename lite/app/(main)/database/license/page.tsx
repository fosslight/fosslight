'use client';

import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';
import { loadingState } from '@/lib/atoms';
import { parseFilters } from '@/lib/filters';
import { useAPI } from '@/lib/hooks';
import ExcelIcon from '@/public/images/excel.png';
import dayjs from 'dayjs';
import Image from 'next/image';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';

export default function LicenseList() {
  const setLoading = useSetRecoilState(loadingState);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Filters
  const filters: { default: List.Filter[]; hidden: List.Filter[] } = {
    default: [
      { label: 'License Name', name: 'licenseName', type: 'char-exact' },
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
        label: 'Obligations',
        name: 'obligations',
        type: 'checkbox',
        options: [
          { label: 'Notice', value: '0' },
          { label: 'Source', value: '1' }
        ]
      },
      {
        label: 'Restrictions',
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
  const filtersQueryParam = queryParams.get('f') || '';
  const filtersForm = useForm({ defaultValues: parseFilters(filtersQueryParam, filters) });

  // Rows/Columns
  const [rows, setRows] = useState<List.License[]>([]);
  const columns: List.Column[] = [
    { name: 'ID', sort: 'LICENSE_ID' },
    { name: 'Name', sort: 'LICENSE_NAME' },
    { name: 'Identifier', sort: 'SHORT_IDENTIFIER' },
    { name: 'Type', sort: 'TYPE' },
    { name: 'Obligations', sort: 'OBLIGATION' },
    { name: 'Restrictions', sort: '' },
    { name: 'URL', sort: 'WEBPAGE' },
    { name: 'Description', sort: 'DESCRIPTION' },
    { name: 'Create', sort: 'CREATED_AT' },
    { name: 'Modify', sort: 'MODIFIED_AT' }
  ];

  // Sorting
  const currentSort = queryParams.get('s') || '';

  // Pagination
  const [totalCount, setTotalCount] = useState(0);
  const countPerPage = 10;
  const currentPage = Number(queryParams.get('p') || '1');

  // API for loading rows
  const loadRowsRequest = useAPI('get', 'http://localhost:8180/api/lite/licenses', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      setTotalCount(res.data.totalCount);
      setRows(res.data.list);
    },
    onFinish: () => setLoading(false)
  });

  // Load new rows when changing page or applying filters (including initial load)
  useEffect(() => {
    loadRowsRequest.execute({
      params: {
        ...filtersForm.watch(),
        sort: currentSort,
        page: currentPage,
        countPerPage
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtersQueryParam, currentSort, currentPage, countPerPage]);

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="breadcrumb">
        Database
        <i className="mx-2 text-sm fa-solid fa-angle-right" />
        License List
      </h2>

      {/* Description */}
      <h3 className="pb-8">List of license information registered in the database.</h3>

      {/* Filters */}
      <ListFilters form={filtersForm} filters={filters} />

      {/* Button */}
      <div className="flex justify-end gap-x-1 mt-8 mb-4">
        <button className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn">
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
        pagination={{ totalCount, currentPage, countPerPage }}
        render={(row: List.License, column: string) => {
          if (column === 'ID') {
            return row.licenseId;
          }

          if (column === 'Name') {
            return row.licenseName;
          }

          if (column === 'Identifier') {
            return row.licenseIdentifier;
          }

          if (column === 'Type') {
            return row.licenseType;
          }

          if (column === 'Obligations') {
            const notice = row.obligations[0] === 'Y';
            const source = row.obligations[1] === 'Y';

            if (!notice && !source) {
              return null;
            }

            return (
              <div className="flex gap-x-2 whitespace-nowrap">
                {notice && <i className="fa-solid fa-file-lines" title="Notice" />}
                {source && <i className="fa-solid fa-code" title="Source" />}
              </div>
            );
          }

          if (column === 'Restrictions') {
            return <div className="whitespace-pre">{row.restrictions.join('\n')}</div>;
          }

          if (column === 'URL') {
            if (!row.homepageUrl) {
              return null;
            }

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
        onClickRow={(row: List.License) => {
          const urlQueryParams = new URLSearchParams(queryParams);
          urlQueryParams.set('modal-type', 'license');
          urlQueryParams.set('modal-id', row.licenseId);
          router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
        }}
      />
    </>
  );
}
