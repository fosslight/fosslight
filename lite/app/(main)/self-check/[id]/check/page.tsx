'use client';

import ListTable from '@/components/list-table';
import { loadingState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

function SelfCheckDetailCheckOSS({ rows }: { rows: SelfCheck.OSSCheck[] }) {
  const [checkedList, setCheckedList] = useState<number[]>([]);
  const [completedList, setCompletedList] = useState<number[]>([]);
  const columns: List.Column[] = [
    { name: 'Download URL', sort: '' },
    { name: 'OSS Name (now)', sort: '' },
    { name: 'OSS Name (to be changed)', sort: '' }
  ];
  const checkbox = { name: 'oss-idx', checkedList, setCheckedList, disabledList: completedList };

  function changeOssName() {
    const validCheckList = checkedList.filter((idx) => !completedList.includes(idx));
    if (validCheckList.length === 0) {
      alert('Nohting to change');
      return;
    }

    if (!window.confirm('Are you sure to continue?')) {
      return;
    }

    const data: any = {};
    rows
      .filter((_, idx) => validCheckList.includes(idx))
      .forEach((row) => {
        row.gridIds.forEach((gridId) => {
          data[gridId] = row.after.value;
        });
      });

    window.opener.postMessage(JSON.stringify({ ossCheck: data }));
    setCompletedList([...completedList, ...validCheckList]);
  }

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
          checkbox={checkbox}
          hideColumnSelector
          render={(row: SelfCheck.OSSCheck, column: string) => {
            if (column === 'Download URL') {
              const urls = row.downloadUrl.split(',');

              return (
                <div className="whitespace-nowrap">
                  {urls.map((url, urlIdx) => (
                    <a
                      key={urlIdx}
                      className="block text-blue-500 hover:underline"
                      href={url}
                      target="_blank"
                      onClick={(e) => e.stopPropagation()}
                    >
                      {url}
                    </a>
                  ))}
                </div>
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

function SelfCheckDetailCheckLicense({ rows }: { rows: SelfCheck.LicenseCheck[] }) {
  const [checkedList, setCheckedList] = useState<number[]>([]);
  const [completedList, setCompletedList] = useState<number[]>([]);
  const columns: List.Column[] = [
    { name: 'OSS Name', sort: '' },
    { name: 'OSS Version', sort: '' },
    { name: 'Download URL', sort: '' },
    { name: 'Licenses (now)', sort: '' },
    { name: 'Licenses (to be changed)', sort: '' }
  ];
  const checkbox = {
    name: 'license-idx',
    checkedList,
    setCheckedList,
    disabledList: completedList
  };

  function changeLicenses() {
    const validCheckList = checkedList.filter((idx) => !completedList.includes(idx));
    if (validCheckList.length === 0) {
      alert('Nohting to change');
      return;
    }

    if (!window.confirm('Are you sure to continue?')) {
      return;
    }

    const data: any = {};
    rows
      .filter((_, idx) => validCheckList.includes(idx))
      .forEach((row) => {
        data[row.gridId] = row.after.value;
      });

    window.opener.postMessage(JSON.stringify({ licenseCheck: data }));
    setCompletedList([...completedList, ...validCheckList]);
  }

  return (
    <>
      <h4 className="mt-16 mb-2 text-lg font-bold">License Check</h4>
      <div className="mb-3 text-sm text-semiblack/80">
        The list of licenses that can be changed to the <b>registered licenses</b>, which are found
        based on the <b>OSS name/version and download URL</b>.
      </div>
      <div className="max-h-[300px] overflow-y-auto no-scrollbar">
        <ListTable
          rows={rows}
          columns={columns}
          checkbox={checkbox}
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
  const setLoading = useSetRecoilState(loadingState);
  const [ossRows, setOssRows] = useState<SelfCheck.OSSCheck[]>([]);
  const [licenseRows, setLicenseRows] = useState<SelfCheck.LicenseCheck[]>([]);

  // API for loading OSS check result
  const checkOssRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${params.id}/oss/check`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => setOssRows(res.data.verificationOss)
    }
  );

  // API for loading license check result
  const checkLicenseRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${params.id}/licenses/check`,
    {
      onSuccess: (res) => setLicenseRows(res.data.verificationLicenses),
      onFinish: () => setLoading(false)
    }
  );

  useEffect(() => {
    checkOssRequest.executeAsync({}).then(() => checkLicenseRequest.execute({}));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <SelfCheckDetailCheckOSS rows={ossRows} />
      <SelfCheckDetailCheckLicense rows={licenseRows} />
    </>
  );
}
