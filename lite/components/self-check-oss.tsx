import { loadingState } from '@/lib/atoms';
import ExcelIcon from '@/public/images/excel.png';
import clsx from 'clsx';
import dayjs from 'dayjs';
import Image from 'next/image';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';
import Toogle from './toggle';

export default function SelfCheckOSS() {
  const setLoading = useSetRecoilState(loadingState);
  const [method, setMethod] = useState<'file' | 'url'>('file');
  const [files, setFiles] = useState<any[]>([]);
  const [ossList, setOssList] = useState<any[]>([]);
  const [excludeList, setExcludeList] = useState<string[]>([]);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setFiles(
        Array.from(Array(2)).map((_, idx) => ({
          name: `uploaded-oss-list-${idx + 1}.zip`,
          when: '2023-09-28 16:17:30'
        }))
      );
      setOssList(
        Array.from(Array(5)).map((_, idx) => ({
          path: 'aaa/bbb',
          ossId: String(5 - idx),
          ossName: 'cairo',
          ossVersion: '1.4.12',
          licenses: 'MPL-1.1, GPL-2.0',
          obligations: 'YY',
          restrictions: ['Non-commercial Use Only', 'Network Copyleft'],
          downloadUrl: 'http://cairographics.org/releases',
          homepageUrl: 'https://www.cairographics.org',
          description: 'Some files in util and test folder are released under GPL-2.0',
          copyright:
            'Copyright (c) 2002 University of Southern California Copyright (c) 2005 Red Hat, Inc.',
          cveId: 'CVE-2020-35492',
          cvssScore: '7.8',
          exclude: false
        }))
      );

      setLoading(false);
    }, 500);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <div className="w-[calc(100%-4px)] shadow-box">
        {/* File vs URL */}
        <div className="flex flex-col items-start gap-y-2 mb-4">
          <label className="flex items-center gap-x-2">
            <input
              type="radio"
              value="file"
              checked={method === 'file'}
              onChange={() => setMethod('file')}
            />
            Upload File (Source Analysis Result)
          </label>
          <label className="flex items-center gap-x-2">
            <input
              type="radio"
              value="url"
              checked={method === 'url'}
              onChange={() => setMethod('url')}
            />
            Enter Source URL
          </label>
        </div>

        {/* Tools for listing up OSS */}
        {method === 'file' ? (
          <div className="p-4 border border-dashed border-semigray rounded text-center">
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
            &ensp;Upload a file here
            <div className="mt-1 text-sm text-darkgray">
              (The file must contain information of OSS to be listed.)
            </div>
          </div>
        ) : (
          <div className="flex gap-x-2">
            <input
              className="flex-1 w-0 px-2 py-1 border border-darkgray outline-none"
              placeholder="The source URL to be analyzed"
            />
            <button className="flex-shrink-0 px-2 py-0.5 crimson-btn">Send</button>
          </div>
        )}
      </div>

      {/* Buttons */}
      <div className="flex justify-between items-center mt-8 mb-2">
        <i className="text-charcoal fa-solid fa-trash" />
        <div className="flex justify-end gap-x-1">
          <button className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn">
            <div className="relative w-4 h-4">
              <Image src={ExcelIcon} fill sizes="32px" alt="export" />
            </div>
            Export
          </button>
          <button className="px-2 py-0.5 default-btn">Bulk Edit</button>
          <button className="px-2 py-0.5 crimson-btn">Check</button>
        </div>
      </div>

      {/* Cards */}
      <div className="grid grid-cols-1 gap-2 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {ossList.map((oss, idx) => (
          <div key={idx} className="flex flex-col gap-y-1 p-4 border border-darkgray rounded">
            <div className="flex justify-between items-center pb-2 mb-1 border-b border-b-darkgray">
              <input className="w-4 h-4" type="checkbox" />
              <span className="text-sm text-darkgray">exclude {oss.exclude ? 'O' : 'X'}</span>
            </div>
            <div className="flex items-center gap-x-2">
              <div
                className="flex gap-x-1 font-semibold cursor-pointer"
                onClick={() => {
                  const urlQueryParams = new URLSearchParams(queryParams);
                  urlQueryParams.set('modal-type', 'oss');
                  urlQueryParams.set('modal-id', oss.ossId);
                  router.push(`${pathname}?${urlQueryParams.toString()}`, {
                    scroll: false
                  });
                }}
              >
                <div className="line-clamp-1 break-all">{oss.ossName}</div>
                <div className="flex-shrink-0">({oss.ossVersion})</div>
              </div>
              <div className="flex items-center gap-x-1 flex-shrink-0 p-1 border border-darkgray rounded text-xs">
                {oss.obligations[0] === 'Y' && (
                  <i className="fa-solid fa-file-lines" title="Notice" />
                )}
                {oss.obligations[1] === 'Y' && <i className="fa-solid fa-code" title="Source" />}
              </div>
              <div
                className="flex-shrink-0 px-1 py-0.5 border border-crimson rounded text-xs text-crimson cursor-pointer"
                onClick={() => {
                  const urlQueryParams = new URLSearchParams(queryParams);
                  urlQueryParams.set('modal-type', 'vuln');
                  urlQueryParams.set('modal-id', oss.cveId);
                  router.push(`${pathname}?${urlQueryParams.toString()}`, {
                    scroll: false
                  });
                }}
              >
                {oss.cvssScore}
              </div>
            </div>
            <div className="text-sm text-semiblack/80">
              <i className="fa-regular fa-folder-open" />
              &ensp;
              {oss.path}
            </div>
            <div className="line-clamp-3 text-sm text-semiblack/80">{oss.licenses}</div>
            <div className="flex items-center gap-x-2 text-sm">
              <i className="text-charcoal fa-solid fa-circle-info" title={oss.description} />
              <i className="text-charcoal fa-solid fa-copyright" title={oss.copyright} />
              <i
                className="text-crimson fa-solid fa-registered"
                title={oss.restrictions.join('\n')}
              />
              <a
                className="text-xs text-blue-500 hover:underline"
                href={oss.downloadUrl}
                target="_blank"
              >
                Download
              </a>
              <a
                className="text-xs text-blue-500 hover:underline"
                href={oss.homepageUrl}
                target="_blank"
              >
                Homepage
              </a>
              <div className="flex-1 text-right">
                <i className="text-sm text-darkgray fa-solid fa-pen" />
              </div>
            </div>
          </div>
        ))}
        <div className="flex justify-center items-center p-4 border border-dashed border-darkgray rounded text-semiblack/80">
          + Add another OSS
        </div>
      </div>
    </>
  );
}
