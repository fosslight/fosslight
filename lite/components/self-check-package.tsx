import { loadingState } from '@/lib/atoms';
import dayjs from 'dayjs';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';
import ListTable from './list-table';

export default function SelfCheckPackage() {
  const setLoading = useSetRecoilState(loadingState);
  const [files, setFiles] = useState<any[]>([]);
  const [rows, setRows] = useState<any[]>([]);
  const columns = [
    { name: 'ID', sort: '' },
    { name: 'Name', sort: '' },
    { name: 'Ver', sort: '' },
    { name: 'License(s)', sort: '' },
    { name: 'URL', sort: '' }
  ];

  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setFiles(
        Array.from(Array(2)).map((_, idx) => ({
          name: `uploaded-package-${idx + 1}.zip`,
          when: '2023-09-28 16:17:30'
        }))
      );
      setRows(
        Array.from(Array(5)).map((_, idx) => ({
          ossId: String(5 - idx),
          ossName: 'cairo',
          ossVersion: '1.4.12',
          licenseName: '(MPL-1.1 AND GPL-2.0) OR (LGPL-2.1 AND GPL-2.0)',
          downloadUrl: 'http://cairographics.org/releases',
          homepageUrl: 'https://www.cairographics.org'
        }))
      );

      setLoading(false);
    }, 500);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* Uploading files */}
      <div className="p-4 mb-8 border border-dashed border-semigray rounded text-center">
        {files.length > 0 && (
          <div className="flex flex-col gap-y-1 mb-6">
            {files.map((file, idx) => (
              <div key={idx} className="flex justify-center items-center gap-x-1.5 text-sm">
                <i className="fa-solid fa-cube" />
                <span className="italic text-semiblack/80">{file.name}</span>
                <span className="text-semiblack/50">
                  ({dayjs(file.when).format('YY.MM.DD HH:mm')})
                </span>
              </div>
            ))}
          </div>
        )}
        <i className="fa-solid fa-arrow-up-from-bracket" />
        &ensp;Upload your package files here
        <div className="mt-1 text-sm text-darkgray">
          (Below are the OSS that must disclose the source codes.&ensp;
          <i className="fa-solid fa-turn-down" />)
        </div>
      </div>

      {/* Table */}
      <ListTable
        rows={rows}
        columns={columns}
        render={(row: any, column: string) => {
          if (column === 'ID') {
            return row.ossId;
          }

          if (column === 'Name') {
            return row.ossName;
          }

          if (column === 'Ver') {
            return row.ossVersion;
          }

          if (column === 'License(s)') {
            return row.licenseName;
          }

          if (column === 'URL') {
            return (
              <div className="whitespace-nowrap">
                <a
                  className="text-blue-500 hover:underline"
                  href={row.downloadUrl}
                  target="_blank"
                  onClick={(e) => e.stopPropagation()}
                >
                  Download
                </a>
                <br />
                <a
                  className="text-blue-500 hover:underline"
                  href={row.homepageUrl}
                  target="_blank"
                  onClick={(e) => e.stopPropagation()}
                >
                  Homepage
                </a>
              </div>
            );
          }

          return null;
        }}
        onClickRow={(row: any) => {
          const urlQueryParams = new URLSearchParams(queryParams);
          urlQueryParams.set('modal-type', 'oss');
          urlQueryParams.set('modal-id', row.ossId);
          router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
        }}
      />
    </>
  );
}
