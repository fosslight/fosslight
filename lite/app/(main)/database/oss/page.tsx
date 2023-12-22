'use client';

import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';
import { loadingState } from '@/lib/atoms';
import { parseFilters } from '@/lib/filters';
import { useAPI } from '@/lib/hooks';
import { OSS_TYPES, serverOrigin } from '@/lib/literals';
import ExcelIcon from '@/public/images/excel.png';
import dayjs from 'dayjs';
import Image from 'next/image';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';

export default function OSSList() {
  const setLoading = useSetRecoilState(loadingState);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Filters
  const [users, setUsers] = useState<{ userName: string; userId: string }[]>([]);
  const filters: { default: List.Filter[]; hidden: List.Filter[] } = {
    default: [
      { label: 'OSS Name', name: 'ossName', type: 'char-exact' },
      { label: 'License Name', name: 'licenseName', type: 'char-exact' },
      { label: 'URL', name: 'url', type: 'char-exact' },
      { label: 'Description', name: 'description', type: 'text' },
      { label: 'Copyright', name: 'copyright', type: 'text' },
      {
        label: 'Deactivate',
        name: 'deactivate',
        type: 'checkbox',
        options: [
          { label: 'NO', value: '0' },
          { label: 'YES', value: '1' }
        ]
      }
    ],
    hidden: [
      {
        label: (
          <span className="flex items-center gap-x-1.5">
            OSS Type
            <i
              className="text-charcoal fa-solid fa-circle-info"
              title={['N: None']
                .concat(Object.entries(OSS_TYPES).map(([key, value]) => `${key}: ${value.name}`))
                .join('\n')}
            />
          </span>
        ),
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
        options: users.map((user) => ({ label: user.userName, value: user.userId }))
      },
      { label: 'Created', name: 'created', type: 'date' },
      {
        label: 'Modifier',
        name: 'modifier',
        type: 'select',
        options: users.map((user) => ({ label: user.userName, value: user.userId }))
      },
      { label: 'Modified', name: 'modified', type: 'date' }
    ]
  };
  const filtersQueryParam = queryParams.get('f') || '';
  const filtersForm = useForm({ defaultValues: parseFilters(filtersQueryParam, filters) });

  // Rows/Columns
  const [rows, setRows] = useState<List.OSS[]>([]);
  const columns: List.Column[] = [
    { name: 'ID', sort: 'OSS_ID' },
    { name: 'Name', sort: 'OSS_NAME' },
    { name: 'Ver', sort: 'OSS_VERSION' },
    { name: 'Type', sort: '' },
    { name: 'Licenses', sort: '' },
    { name: 'Obligations', sort: '' },
    { name: 'URL', sort: '' },
    { name: 'Description', sort: '' },
    { name: 'Vuln', sort: 'CVSS_SCORE' },
    { name: 'Create', sort: 'CREATED_AT' },
    { name: 'Modify', sort: 'MODIFIED_AT' }
  ];

  // Sorting
  const currentSort = queryParams.get('s') || '';

  // Pagination
  const [totalCount, setTotalCount] = useState(0);
  const countPerPage = 10;
  const currentPage = Number(queryParams.get('p') || '1');

  // API for loading users
  const loadUsersRequest = useAPI('get', '/api/lite/users', {
    onSuccess: (res) => setUsers(res.data)
  });

  // API for loading rows
  const loadRowsRequest = useAPI('get', '/api/lite/oss', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      setTotalCount(res.data.totalCount);
      setRows(res.data.list);
    },
    onFinish: () => setLoading(false)
  });

  // API for exporting
  const downloadExcelRequest = useAPI('get', '/api/lite/oss/export/excel', {
    onSuccess: (res) => {
      window.location.href = `${serverOrigin}/exceldownload/getFile?id=${res.data}`;
    }
  });

  // Load new rows when changing page or applying filters (including initial load)
  useEffect(() => {
    loadUsersRequest.execute({});
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
        OSS List
      </h2>

      {/* Description */}
      <h3 className="pb-8">
        List of OSS(= Open Source Software) information registered in the database.
      </h3>

      {/* Filters */}
      <ListFilters form={filtersForm} filters={filters} />

      {/* Button */}
      <div className="flex justify-end gap-x-1 mt-8 mb-4">
        <button
          className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn"
          onClick={() =>
            downloadExcelRequest.execute({
              params: { ...filtersForm.watch() }
            })
          }
        >
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
        render={(row: List.OSS, column: string) => {
          if (column === 'ID') {
            return row.ossId;
          }

          if (column === 'Name') {
            return row.ossName;
          }

          if (column === 'Ver') {
            return row.ossVersion;
          }

          if (column === 'Type') {
            return (
              <div className="whitespace-nowrap">
                {(() => {
                  const idxToKey = Object.fromEntries(
                    Object.keys(OSS_TYPES).map((key, idx) => [idx, key])
                  );

                  return (
                    <div className="flex gap-x-1">
                      {row.ossType.split('').map((x, idx) => {
                        if (x !== '1') {
                          return null;
                        }

                        const key = idxToKey[idx];
                        const typeInfo = OSS_TYPES[key];

                        return (
                          <span
                            key={key}
                            className="px-1 rounded text-xs text-semiwhite"
                            style={{ backgroundColor: typeInfo.color }}
                            title={`[${typeInfo.name}] ${typeInfo.desc}`}
                          >
                            {key}
                          </span>
                        );
                      })}
                    </div>
                  );
                })()}
              </div>
            );
          }

          if (column === 'Licenses') {
            return `${row.licenseType ? `[${row.licenseType}] ` : ''}${row.licenseName}`;
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

          if (column === 'URL') {
            return (
              <div className="whitespace-nowrap">
                {row.downloadUrl && (
                  <a
                    className="block text-blue-500 hover:underline"
                    href={row.downloadUrl}
                    target="_blank"
                    onClick={(e) => e.stopPropagation()}
                  >
                    Download
                  </a>
                )}
                {row.homepageUrl && (
                  <a
                    className="block text-blue-500 hover:underline"
                    href={row.homepageUrl}
                    target="_blank"
                    onClick={(e) => e.stopPropagation()}
                  >
                    Homepage
                  </a>
                )}
              </div>
            );
          }

          if (column === 'Description') {
            return <div className="whitespace-pre-line">{row.description}</div>;
          }

          if (column === 'Vuln') {
            if (!row.cveId || !row.cvssScore) {
              return null;
            }

            return (
              <a
                className="text-crimson hover:underline"
                href={`https://nvd.nist.gov/vuln/detail/${row.cveId}`}
                target="_blank"
                onClick={(e) => e.stopPropagation()}
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
        onClickRow={(row: List.OSS) => {
          const urlQueryParams = new URLSearchParams(queryParams);
          urlQueryParams.set('modal-type', 'oss');
          urlQueryParams.set('modal-id', row.ossId);
          router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
        }}
      />
    </>
  );
}
