import { loadingState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import dayjs from 'dayjs';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';
import ListTable from './list-table';

export default function SelfCheckPackage({ id }: { id: string }) {
  const setLoading = useSetRecoilState(loadingState);
  const [fileList, setFileList] = useState<SelfCheck.PackageFile[]>([]);
  const [ossList, setOssList] = useState<SelfCheck.PackageOSS[]>([]);
  const columns = [
    { name: 'ID', sort: '' },
    { name: 'Name', sort: '' },
    { name: 'Ver', sort: '' },
    { name: 'Licenses', sort: '' },
    { name: 'URL', sort: '' }
  ];

  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // API for loading file list
  const loadFilesRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${id}/packages/files`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => setFileList(res.data.files),
      onFinish: () => setLoading(false)
    }
  );

  // API for loading OSS list
  const loadRowsRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${id}/packages`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => setOssList(res.data.oss),
      onFinish: () => setLoading(false)
    }
  );

  // API for uploading file
  const uploadFileRequest = useAPI(
    'post',
    `http://localhost:8180/api/lite/selfchecks/${id}/packages/files`,
    {
      onSuccess: () => loadFilesRequest.execute({}),
      type: 'file'
    }
  );

  useEffect(() => {
    loadFilesRequest.execute({});
    loadRowsRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* Uploading files */}
      <div className="p-4 mb-12 border border-dashed border-semigray rounded text-center">
        {fileList.length > 0 && (
          <div className="flex flex-col items-center gap-y-1 mb-6">
            {fileList.map((file, idx) => (
              <a
                key={idx}
                className="flex justify-center items-center gap-x-1.5 text-sm"
                href={`http://localhost:8180/download/${file.fileSeq}/${file.logiNm}`}
                target="_blank"
              >
                <i className="fa-solid fa-cube" />
                <span className="italic text-semiblack/80">{file.orgNm}</span>
                <span className="text-semiblack/50">
                  ({dayjs(file.created).format('YY.MM.DD HH:mm')})
                </span>
                <i
                  className="px-0.5 ml-1.5 text-xs text-crimson cursor-pointer fa-solid fa-x"
                  onClick={(e) => {
                    e.preventDefault();
                    // deleteFileRequest.execute({ body: { seq: file.fileSeq } });
                  }}
                />
              </a>
            ))}
          </div>
        )}
        <span
          className="cursor-pointer"
          onClick={() => {
            document.getElementById('upload-file')?.click();
          }}
        >
          <i className="fa-solid fa-arrow-up-from-bracket" />
          &ensp;Upload your package files here
        </span>
        <div className="mt-1 text-sm text-darkgray">
          (Below are the OSS that must disclose the source codes.&ensp;
          <i className="fa-solid fa-turn-down" />)
        </div>
        <input
          id="upload-file"
          className="hidden"
          type="file"
          accept=".zip, .tar.gz, .gz"
          onChange={(e) => {
            const input = e.target;

            if (!input.files || input.files.length === 0) {
              return;
            }

            const file = input.files[0];
            const allowedExtensions = /\.(zip|tar\.gz|gz)$/i;
            if (!allowedExtensions.test(file.name)) {
              alert('Select a file with valid extension(zip, tar.gz, or gz)');
              input.value = '';
              return;
            }

            const formData = new FormData();
            formData.append('selfCheckPackageFile', file, file.name);

            uploadFileRequest.execute({ body: formData });
            input.value = '';
          }}
        />
      </div>

      {/* Table */}
      <ListTable
        rows={ossList}
        columns={columns}
        render={(row: SelfCheck.PackageOSS, column: string) => {
          if (column === 'ID') {
            return row.ossId;
          }

          if (column === 'Name') {
            return row.ossName;
          }

          if (column === 'Ver') {
            return row.ossVersion;
          }

          if (column === 'Licenses') {
            return row.licenseName;
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

          return null;
        }}
        onClickRow={(row: SelfCheck.PackageOSS) => {
          const urlQueryParams = new URLSearchParams(queryParams);
          urlQueryParams.set('modal-type', 'oss');
          urlQueryParams.set('modal-id', row.ossId);
          router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
        }}
      />
    </>
  );
}
