'use client';

import ListFilters from '@/components/list-filters';
import ListTable from '@/components/list-table';
import SelfCheckModal from '@/components/self-check-modal';
import SelfCheckNoticeWarningModal from '@/components/self-check-notice-warning-modal';
import { loadingState } from '@/lib/atoms';
import { parseFilters } from '@/lib/filters';
import { useAPI } from '@/lib/hooks';
import { serverOrigin } from '@/lib/literals';
import dayjs from 'dayjs';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';

export default function SelfCheckList() {
  const setLoading = useSetRecoilState(loadingState);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Filters
  const filters: { default: List.Filter[]; hidden: List.Filter[] } = {
    default: [
      { label: 'Project ID', name: 'projectId', type: 'number' },
      { label: 'Project Name', name: 'projectName', type: 'char-exact' },
      { label: 'OSS Name', name: 'ossName', type: 'char-exact' },
      { label: 'License Name', name: 'licenseName', type: 'char-exact' },
      { label: 'Created', name: 'created', type: 'date' }
    ],
    hidden: []
  };
  const filtersQueryParam = queryParams.get('f') || '';
  const filtersForm = useForm({ defaultValues: parseFilters(filtersQueryParam, filters) });

  // Modal
  const [isModalShown, setIsModalShown] = useState(false);

  // Rows/Columns
  const [rows, setRows] = useState<List.SelfCheck[]>([]);
  const columns: List.Column[] = [
    { name: 'ID', sort: 'PRJ_ID' },
    { name: 'Name', sort: 'PRJ_NAME' },
    { name: 'Ver', sort: 'PRJ_VERSION' },
    { name: 'Report', sort: '' },
    { name: 'Packages', sort: '' },
    { name: 'Notice', sort: '' },
    { name: 'Vuln', sort: '' },
    { name: 'Create', sort: 'CREATED_DATE' }
  ];

  // Sorting
  const currentSort = queryParams.get('s') || '';

  // Pagination
  const [totalCount, setTotalCount] = useState(0);
  const countPerPage = 10;
  const currentPage = Number(queryParams.get('p') || '1');

  // API for loading rows
  const loadRowsRequest = useAPI('get', '/api/lite/selfchecks', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      setTotalCount(res.data.totalCount);
      setRows(res.data.list);
    },
    onFinish: () => setLoading(false)
  });

  // API for downloading report
  const downloadReportRequest = useAPI('post', '/exceldownload/getExcelPost', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      window.location.href = `${serverOrigin}/exceldownload/getFile?id=${res.data.validMsg}`;
    },
    onFinish: () => setLoading(false),
    type: 'json'
  });

  // API for validating notice
  const [selfcheckId, setSelfcheckId] = useState('');
  const validateNoticeRequest = useAPI('get', `/selfCheck/ossGrid/${selfcheckId}/10`);

  // API for sending email
  const [isWarningShown, setIsWarningShown] = useState(false);
  const sendEmailRequest = useAPI(
    'post',
    `/api/lite/selfchecks/${selfcheckId}/license-notice-email`,
    {
      onStart: () => setLoading(true),
      onSuccess: () => {
        alert('Successfully sent email');
        setIsWarningShown(false);
      },
      onError: () => alert('Failed in sending email'),
      onFinish: () => setLoading(false)
    }
  );

  // API for downloading notice
  const downloadNoticeRequest = useAPI('post', '/selfCheck/makeNoticePreview', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      window.location.href = `${serverOrigin}/selfCheck/downloadNoticePreview?id=${res.data.validMsg}`;
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
      <h2 className="breadcrumb">Self-Check Project List</h2>

      {/* Description */}
      <h3 className="pb-8">List of projects you&apos;ve created for self-check.</h3>

      {/* Filters */}
      <ListFilters form={filtersForm} filters={filters} />

      {/* Button */}
      <div className="flex justify-end mt-8 mb-4">
        <button
          className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn"
          onClick={() => setIsModalShown(true)}
        >
          <i className="text-sm fa-solid fa-plus" /> Create Project
        </button>
      </div>
      <SelfCheckModal show={isModalShown} onHide={() => setIsModalShown(false)} />

      {/* Table (Rows/Columns + Sorting + Pagination) */}
      <ListTable
        rows={rows}
        columns={columns}
        currentSort={currentSort}
        pagination={{ totalCount, currentPage, countPerPage }}
        render={(row: List.SelfCheck, column: string) => {
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
              row.ossCount > 0 && (
                <i
                  className="cursor-pointer fa-regular fa-file-excel"
                  title="Download FOSSLight Report"
                  onClick={(e) => {
                    e.stopPropagation();
                    downloadReportRequest.execute({
                      body: { parameter: row.projectId, type: 'selfReport' }
                    });
                  }}
                />
              )
            );
          }

          if (column === 'Packages') {
            return (
              row.packages.length > 0 && (
                <div className="flex gap-x-2 whitespace-nowrap">
                  {row.packages.map((file, idx) => (
                    <a
                      key={idx}
                      href={`${serverOrigin}/download/${file.fileSeq}/${file.logiNm}`}
                      target="_blank"
                      title={`Download ${file.orgNm}`}
                      onClick={(e) => e.stopPropagation()}
                    >
                      <i className="fa-solid fa-cube" />
                    </a>
                  ))}
                </div>
              )
            );
          }

          if (column === 'Notice') {
            return (
              <i
                className="cursor-pointer fa-solid fa-file-lines"
                title="Download Notice"
                onClick={(e) => {
                  e.stopPropagation();

                  setSelfcheckId(row.projectId);
                  setTimeout(() => {
                    validateNoticeRequest
                      .executeAsync({ params: { referenceId: row.projectId } })
                      .then((res) => {
                        const { validData } = res.data;
                        let isValid = true;

                        if (validData) {
                          const keys = Object.keys(validData);
                          if (keys.some((key) => key.startsWith('licenseName'))) {
                            isValid = false;
                          }
                        }

                        if (isValid) {
                          downloadNoticeRequest.execute({
                            body: {
                              prjId: row.projectId,
                              previewOnly: 'N',
                              isSimpleNotice: 'N'
                            }
                          });
                        } else {
                          setIsWarningShown(true);
                        }
                      });
                  }, 0);
                }}
              />
            );
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
            return dayjs(row.created.substring(0, 10)).format('YY.MM.DD');
          }

          return null;
        }}
        onClickRow={(row: List.SelfCheck) => router.push(`${pathname}/${row.projectId}`)}
      />

      {/* Warning */}
      <SelfCheckNoticeWarningModal
        show={isWarningShown}
        onHide={() => setIsWarningShown(false)}
        sendEmail={() => sendEmailRequest.execute({})}
      />
    </>
  );
}
