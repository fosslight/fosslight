'use client';

import ListTable from '@/components/list-table';
import { loadingState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

function SelfCheckDetailCheckOSS({ id }: { id: string }) {
  const setLoading = useSetRecoilState(loadingState);

  // Rows/Columns
  const [rows, setRows] = useState<SelfCheck.OSSCheck[]>([]);
  const columns: List.Column[] = [
    { name: 'Download URL', sort: '' },
    { name: 'OSS Name (now)', sort: '' },
    { name: 'OSS Name (to be changed)', sort: '' }
  ];

  // API for loading OSS check result
  const checkOssRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${id}/oss/check`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => setRows(res.data.verificationOss),
      onFinish: () => setLoading(false)
    }
  );

  function changeOssName() {
    const data: any = {};
    rows.forEach((row) => {
      row.gridIds.forEach((gridId) => {
        data[gridId] = row.after.value;
      });
    });

    if (Object.keys(data).length === 0) {
      alert('Nohting to change');
      return;
    }

    window.opener.postMessage(JSON.stringify({ ossCheck: data }));
  }

  useEffect(() => {
    checkOssRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <h4 className="mb-2 text-lg font-bold">OSS Check</h4>
      <div className="mb-3 text-sm text-semiblack/80">
        The list of OSS whose name can be changed to the <b>registered OSS name</b>, which is found
        based on the <b>download URL</b>.
      </div>
      <div className="max-h-[300px] overflow-y-auto no-scrollbar">
        <ListTable
          rows={rows}
          columns={columns}
          hideColumnSelector
          render={(row: SelfCheck.OSSCheck, column: string) => {
            if (column === 'Download URL') {
              return (
                <a
                  className="text-blue-500 whitespace-nowrap hover:underline"
                  href={row.downloadUrl}
                  target="_blank"
                  onClick={(e) => e.stopPropagation()}
                >
                  {row.downloadUrl}
                </a>
              );
            }

            if (column === 'OSS Name (now)') {
              return (
                <>
                  {row.before.value}
                  {row.before.msg && (
                    <div className="mt-1 text-xs text-red-500">{row.before.msg}</div>
                  )}
                </>
              );
            }

            if (column === 'OSS Name (to be changed)') {
              return (
                <>
                  {row.after.value}
                  {row.after.msg && (
                    <div className="mt-1 text-xs text-red-500">{row.after.msg}</div>
                  )}
                </>
              );
            }

            return null;
          }}
        />
      </div>
      {rows.length > 0 && (
        <div className="mt-3 text-right">
          <button className="px-2 py-0.5 crimson-btn" onClick={changeOssName}>
            Change OSS Name
          </button>
        </div>
      )}
    </>
  );
}

function SelfCheckDetailCheckLicense({ id }: { id: string }) {
  const setLoading = useSetRecoilState(loadingState);

  // Rows/Columns
  const [rows, setRows] = useState<SelfCheck.LicenseCheck[]>([]);
  const columns: List.Column[] = [
    { name: 'OSS Name', sort: '' },
    { name: 'OSS Version', sort: '' },
    { name: 'Download URL', sort: '' },
    { name: 'Licenses (now)', sort: '' },
    { name: 'Licenses (to be changed)', sort: '' }
  ];

  // API for loading license check result
  const checkLicenseRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${id}/licenses/check`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => setRows(res.data.verificationLicenses),
      onFinish: () => setLoading(false)
    }
  );

  function changeLicenses() {
    const data: any = {};
    rows.forEach((row) => {
      data[row.gridId] = row.after.value;
    });

    if (Object.keys(data).length === 0) {
      alert('Nohting to change');
      return;
    }

    window.opener.postMessage(JSON.stringify({ licenseCheck: data }));
  }

  useEffect(() => {
    checkLicenseRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <h4 className="mb-2 text-lg font-bold">License Check</h4>
      <div className="mb-3 text-sm text-semiblack/80">
        The list of licenses that can be changed to the <b>registered licenses</b>, which are found
        based on the <b>OSS name/version and download URL</b>.
      </div>
      <div className="max-h-[300px] overflow-y-auto no-scrollbar">
        <ListTable
          rows={rows}
          columns={columns}
          hideColumnSelector
          render={(row: SelfCheck.LicenseCheck, column: string) => {
            if (column === 'OSS Name') {
              return row.ossName;
            }

            if (column === 'OSS Version') {
              return row.ossVersion;
            }

            if (column === 'Download URL') {
              return (
                <a
                  className="text-blue-500 whitespace-nowrap hover:underline"
                  href={row.downloadUrl}
                  target="_blank"
                  onClick={(e) => e.stopPropagation()}
                >
                  {row.downloadUrl}
                </a>
              );
            }

            if (column === 'Licenses (now)') {
              return (
                <>
                  {row.before.value.join(', ')}
                  {row.before.msg && (
                    <div className="mt-1 text-xs text-red-500">{row.before.msg}</div>
                  )}
                </>
              );
            }

            if (column === 'Licenses (to be changed)') {
              return (
                <>
                  {row.after.value.join(', ')}
                  {row.after.msg && (
                    <div className="mt-1 text-xs text-red-500">{row.after.msg}</div>
                  )}
                </>
              );
            }

            return null;
          }}
        />
      </div>
      {rows.length > 0 && (
        <div className="mt-3 text-right">
          <button className="px-2 py-0.5 crimson-btn" onClick={changeLicenses}>
            Change Licenses
          </button>
        </div>
      )}
    </>
  );
}

export default function SelfCheckDetailCheck({ params }: { params: { id: string } }) {
  return (
    <>
      <SelfCheckDetailCheckOSS id={params.id} />
      <hr className="my-8 border-semigray" />
      <SelfCheckDetailCheckLicense id={params.id} />
    </>
  );
}
