import { loadingState } from '@/lib/atoms';
import { highlight } from '@/lib/commons';
import { restrictions } from '@/lib/literals';
import ExcelIcon from '@/public/images/excel.png';
import clsx from 'clsx';
import dayjs from 'dayjs';
import Image from 'next/image';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { FieldValues, useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';
import SelfCheckOSSFilters from './self-check-oss-filters';
import SelfCheckOSSModal from './self-check-oss-modal';
import SelfCheckOSSPagination from './self-check-oss-pagination';
import Toogle from './toggle';

export default function SelfCheckOSS() {
  const setLoading = useSetRecoilState(loadingState);
  const [method, setMethod] = useState<'file' | 'url'>('file');
  const [files, setFiles] = useState<SelfCheck.OSSFile[]>([]);
  const [ossList, setOssList] = useState<SelfCheck.OSS[]>([]);
  const [excludeList, setExcludeList] = useState<string[]>([]);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Modal
  const [modalData, setModalData] = useState<SelfCheck.SetOSS>();
  const [isModalShown, setIsModalShown] = useState(false);

  // Filters
  const filtersForm = useForm();
  const [filters, setFilters] = useState({ path: '', keyword: '', url: '', copyright: '' });
  const filteredOssList = ossList.filter((oss) => {
    let { path, keyword, url, copyright } = filters;

    if (!path && !keyword && !url && !copyright) {
      return true;
    }

    path = path.toLowerCase();
    keyword = keyword.toLowerCase();
    url = url.toLowerCase();
    copyright = copyright.toLowerCase();

    let result = false;

    if (path) {
      result = result || oss.path.toLowerCase().includes(path);
    }

    if (keyword) {
      result =
        result ||
        oss.ossName.toLowerCase().includes(keyword) ||
        oss.licenses.some((license) => license.licenseIdentifier.toLowerCase().includes(keyword));
    }

    if (url) {
      result =
        result ||
        oss.downloadUrl.toLowerCase().includes(url) ||
        oss.homepageUrl.toLowerCase().includes(url);
    }

    if (copyright) {
      result = result || oss.copyright.toLowerCase().includes(copyright);
    }

    return result;
  });

  // Sorting
  const [sort, setSort] = useState<{ key: string; asc: boolean }[]>([]);
  sort.reverse().forEach(({ key, asc }) => {
    filteredOssList.sort((a, b) => {
      const x = asc ? a : b;
      const y = asc ? b : a;

      if (key === 'path') {
        return x.path.localeCompare(y.path);
      }
      if (key === 'oss') {
        return x.ossName.localeCompare(y.ossName);
      }
      if (key === 'license') {
        const xLicenses = a.licenses.map((license) => license.licenseIdentifier).join(', ');
        const yLicenses = b.licenses.map((license) => license.licenseIdentifier).join(', ');
        return xLicenses.localeCompare(yLicenses);
      }
      if (key === 'download') {
        return x.downloadUrl.localeCompare(y.downloadUrl);
      }
      if (key === 'homepage') {
        return x.homepageUrl.localeCompare(y.homepageUrl);
      }
      return 0;
    });
  });

  // Pagination
  const countPerPage = 3;
  const [currentPage, setCurrentPage] = useState(1);
  const lastPage = Math.max(Math.ceil(filteredOssList.length / countPerPage), 1);
  const paginatedOssList = filteredOssList.slice(
    (currentPage - 1) * countPerPage,
    currentPage * countPerPage
  );

  function openSubwindowForCheck() {
    const [width, height] = [900, 600];
    const left = window.innerWidth / 2 - width / 2;
    const top = window.innerHeight / 2 - height / 2;

    window.open(
      `${pathname}/check?`,
      'selfCheckDetailCheck',
      `width=${width}, height=${height}, left=${left}, top=${top}`
    );
  }

  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setFiles(
        Array.from(Array(2)).map((_, idx) => ({
          name: `uploaded-oss-list-${idx + 1}.zip`,
          when: '2023-09-28 16:17:30'
        }))
      );

      const data = Array.from(Array(5)).map((_, idx) => ({
        path: 'aaa/bbb',
        ossId: String(5 - idx),
        ossName: 'cairo',
        ossVersion: '1.4.12',
        licenses: [
          { licenseId: '1', licenseIdentifier: 'MPL-1.1' },
          { licenseId: '2', licenseIdentifier: 'GPL-2.0' }
        ],
        obligations: ['Y', 'Y'],
        restrictions: ['1', '2'],
        downloadUrl: 'http://cairographics.org/releases',
        homepageUrl: 'https://www.cairographics.org',
        description: 'Some files in util and test folder are released under GPL-2.0',
        copyright:
          'Copyright (c) 2002 University of Southern California Copyright (c) 2005 Red Hat, Inc.',
        cveId: 'CVE-2020-35492',
        cvssScore: '7.8',
        exclude: false
      }));

      setOssList(data);
      setExcludeList(data.filter((oss) => oss.exclude).map((oss) => oss.ossId));

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
      <div className="flex justify-between items-center mt-12 mb-2">
        <div className="flex gap-x-3 text-charcoal">
          <i className="cursor-pointer fa-solid fa-trash" />
          <i
            className="cursor-pointer fa-solid fa-plus"
            onClick={() => {
              setModalData(undefined);
              setIsModalShown(true);
            }}
          />
        </div>
        <div className="flex justify-end gap-x-1">
          <button className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn">
            <div className="relative w-4 h-4">
              <Image src={ExcelIcon} fill sizes="32px" alt="export" />
            </div>
            Export
          </button>
          <button className="px-2 py-0.5 default-btn" onClick={openSubwindowForCheck}>
            Check
          </button>
          <button className="px-2 py-0.5 crimson-btn">Save</button>
        </div>
      </div>

      {/* Cards */}
      <div className="border border-charcoal rounded overflow-hidden">
        <div className="py-2 bg-charcoal font-semibold text-semiwhite text-center">
          <i className="fa-solid fa-list" />
          &ensp; OSS List ({filteredOssList.length})
        </div>
        <SelfCheckOSSFilters
          form={filtersForm}
          onSubmit={(filterParams: FieldValues) => {
            const { path, keyword, url, copyright } = filterParams;

            // Filters
            setFilters({ path, keyword, url, copyright });
            setCurrentPage(1);

            // Sorting
            setSort(filterParams.sort);
          }}
        />
        {paginatedOssList.length > 0 ? (
          <div className="grid grid-cols-1 gap-2 max-h-[700px] p-4 overflow-y-auto no-scrollbar md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {paginatedOssList.map((oss) => (
              <div
                key={oss.ossId}
                className={clsx(
                  'flex flex-col gap-y-1 p-4 border border-gray rounded',
                  excludeList.includes(oss.ossId) && 'bg-semigray/50'
                )}
              >
                <div className="flex justify-between items-center pb-2 mb-1 border-b border-b-darkgray">
                  <input className="w-4 h-4" type="checkbox" />
                  <label className="flex items-center gap-x-2">
                    <span
                      className={clsx(
                        'text-sm',
                        excludeList.includes(oss.ossId) ? 'text-crimson' : 'text-darkgray'
                      )}
                    >
                      exclude
                    </span>
                    <Toogle
                      icons={false}
                      checked={excludeList.includes(oss.ossId)}
                      onChange={() => {
                        if (excludeList.includes(oss.ossId)) {
                          setExcludeList(excludeList.filter((ossId) => ossId !== oss.ossId));
                        } else {
                          setExcludeList([...excludeList, oss.ossId]);
                        }
                      }}
                    />
                  </label>
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
                    <div className="line-clamp-1 break-all">
                      <span
                        dangerouslySetInnerHTML={{
                          __html: highlight(oss.ossName, filters.keyword)
                        }}
                      />
                    </div>
                    {oss.ossVersion && <div className="flex-shrink-0">({oss.ossVersion})</div>}
                  </div>
                  {(oss.obligations[0] === 'Y' || oss.obligations[1] === 'Y') && (
                    <div className="flex items-center gap-x-1 flex-shrink-0 p-1 border border-darkgray rounded text-xs">
                      {oss.obligations[0] === 'Y' && (
                        <i className="fa-solid fa-file-lines" title="Notice" />
                      )}
                      {oss.obligations[1] === 'Y' && (
                        <i className="fa-solid fa-code" title="Source" />
                      )}
                    </div>
                  )}
                  {oss.cveId && oss.cvssScore && (
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
                  )}
                </div>
                <div className="text-sm text-semiblack/80">
                  <i className="fa-regular fa-folder-open" />
                  &ensp;
                  <span
                    dangerouslySetInnerHTML={{
                      __html: highlight(oss.path, filters.path)
                    }}
                  />
                </div>
                {oss.licenses.length > 0 && (
                  <div className="line-clamp-3 text-sm text-semiblack/80">
                    <span
                      dangerouslySetInnerHTML={{
                        __html: oss.licenses
                          .map((license) => highlight(license.licenseIdentifier, filters.keyword))
                          .join(', ')
                      }}
                    />
                  </div>
                )}
                <div className="flex items-center gap-x-2 text-sm">
                  <i className="text-charcoal fa-solid fa-circle-info" title={oss.description} />
                  <i className="text-charcoal fa-solid fa-copyright" title={oss.copyright} />
                  <i
                    className="text-crimson fa-solid fa-registered"
                    title={(() => {
                      const idToDisplay = Object.fromEntries(restrictions);
                      return oss.restrictions.map((id) => idToDisplay[id]).join('\n');
                    })()}
                  />
                  {oss.downloadUrl && (
                    <a
                      className="text-xs text-blue-500 hover:underline"
                      href={oss.downloadUrl}
                      target="_blank"
                    >
                      Download
                    </a>
                  )}
                  {oss.homepageUrl && (
                    <a
                      className="text-xs text-blue-500 hover:underline"
                      href={oss.homepageUrl}
                      target="_blank"
                    >
                      Homepage
                    </a>
                  )}
                  <div className="flex-1 text-right">
                    <i
                      className="text-sm text-darkgray cursor-pointer fa-solid fa-pen"
                      onClick={() => {
                        setModalData({
                          path: oss.path,
                          ossName: oss.ossName,
                          ossVersion: oss.ossVersion,
                          licenses: oss.licenses,
                          downloadUrl: oss.downloadUrl,
                          homepageUrl: oss.homepageUrl,
                          copyright: oss.copyright
                        });
                        setIsModalShown(true);
                      }}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-4 mb-4 text-darkgray text-center">No entries</div>
        )}
      </div>
      <SelfCheckOSSPagination
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
        lastPage={lastPage}
      />

      {/* Modal */}
      <SelfCheckOSSModal
        mode={!modalData ? 'add' : 'edit'}
        data={modalData}
        show={isModalShown}
        onHide={() => setIsModalShown(false)}
      />
    </>
  );
}
