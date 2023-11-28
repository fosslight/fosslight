'use client';

import ListTable from '@/components/list-table';
import { loadingState } from '@/lib/atoms';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

function SelfCheckDetailCheckOSS() {
  const setLoading = useSetRecoilState(loadingState);

  // Rows/Columns
  const [rows, setRows] = useState<SelfCheck.OSSCheck[]>([]);
  const columns: List.Column[] = [
    { name: 'Download URL', sort: '' },
    { name: 'OSS Name (now)', sort: '' },
    { name: 'OSS Name (to be changed)', sort: '' }
  ];

  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setRows(
        Array.from(Array(10)).map(() => ({
          downloadUrl: 'http://cairographics.org/releases',
          ossName: 'cair',
          newOssName: 'cairo'
        }))
      );

      setLoading(false);
    }, 500);
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
              return row.ossName;
            }

            if (column === 'OSS Name (to be changed)') {
              return row.newOssName;
            }

            return null;
          }}
        />
      </div>
      <div className="mt-3 text-right">
        <button className="px-2 py-0.5 crimson-btn">Change OSS Name</button>
      </div>
    </>
  );
}

function SelfCheckDetailCheckLicense() {
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

  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setRows(
        Array.from(Array(10)).map(() => ({
          ossName: 'cairo',
          ossVersion: '1.0.0',
          downloadUrl: 'http://cairographics.org/releases',
          licenses: ['MPL-1.1', 'GPL-2.0'],
          newLicenses: ['Apache-2.0']
        }))
      );

      setLoading(false);
    }, 500);
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
              return row.licenses.join(', ');
            }

            if (column === 'Licenses (to be changed)') {
              return row.newLicenses.join(', ');
            }

            return null;
          }}
        />
      </div>
      <div className="mt-3 text-right">
        <button className="px-2 py-0.5 crimson-btn">Change Licenses</button>
      </div>
    </>
  );
}

export default function SelfCheckDetailCheck() {
  return (
    <>
      <SelfCheckDetailCheckOSS />
      <hr className="my-8 border-semigray" />
      <SelfCheckDetailCheckLicense />
    </>
  );
}
