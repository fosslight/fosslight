import { loadingState } from '@/lib/atoms';
import { highlight } from '@/lib/commons';
import { useAPI } from '@/lib/hooks';
import ExcelIcon from '@/public/images/excel.png';
import clsx from 'clsx';
import dayjs from 'dayjs';
import Image from 'next/image';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { FieldValues, useForm } from 'react-hook-form';
import { useSetRecoilState } from 'recoil';
import Modal from './modal';
import SelfCheckOSSFilters from './self-check-oss-filters';
import SelfCheckOSSModal from './self-check-oss-modal';
import SelfCheckOSSPagination from './self-check-oss-pagination';
import Toogle from './toggle';

export default function SelfCheckOSS({ id }: { id: string }) {
  const setLoading = useSetRecoilState(loadingState);
  const [method, setMethod] = useState<'file' | 'url'>('file');
  const [files, setFiles] = useState<SelfCheck.OSSFile[]>([]);
  const [ossList, setOssList] = useState<SelfCheck.OSS[]>([]);
  const [excludeList, setExcludeList] = useState<string[]>([]);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Modals
  const [editValues, setEditValues] = useState<SelfCheck.EditOSS>();
  const [isOSSModalShown, setIsOSSModalShown] = useState(false);
  const [modalTitle, setModalTitle] = useState('');
  const [modalText, setModalText] = useState('');
  const [isTextModalShown, setIsTextModalShown] = useState(false);

  // Filters
  const filtersForm = useForm();
  const [filters, setFilters] = useState({ keyword: '', path: '', copyright: '', url: '' });
  const filteredOssList = ossList.filter((oss) => {
    let { keyword, path, copyright, url } = filters;

    if (!keyword && !path && !copyright && !url) {
      return true;
    }

    keyword = keyword.toLowerCase();
    path = path.toLowerCase();
    copyright = copyright.toLowerCase();
    url = url.toLowerCase();

    let result = false;

    if (keyword) {
      result =
        result ||
        oss.ossName.toLowerCase().includes(keyword) ||
        oss.licenses.some((license) => license.licenseName.toLowerCase().includes(keyword));
    }

    if (path) {
      result = result || oss.path.toLowerCase().includes(path);
    }

    if (copyright) {
      result = result || oss.copyright.toLowerCase().includes(copyright);
    }

    if (url) {
      result =
        result ||
        oss.downloadUrl.toLowerCase().includes(url) ||
        oss.homepageUrl.toLowerCase().includes(url);
    }

    return result;
  });

  // Sorting
  const [sort, setSort] = useState<{ key: string; asc: boolean }[]>([]);
  sort.reverse().forEach(({ key, asc }) => {
    filteredOssList.sort((a, b) => {
      const x = asc ? a : b;
      const y = asc ? b : a;

      if (key === 'oss') {
        return x.ossName.localeCompare(y.ossName);
      }
      if (key === 'license') {
        const xLicenses = a.licenses.map((license) => license.licenseName).join(', ');
        const yLicenses = b.licenses.map((license) => license.licenseName).join(', ');
        return xLicenses.localeCompare(yLicenses);
      }
      if (key === 'path') {
        return x.path.localeCompare(y.path);
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
  const countPerPage = 200;
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

  // API for loading OSS list
  const loadOssListRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${id}/list-oss`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => {
        setOssList(res.data.list);
        setExcludeList(
          res.data.list
            .filter((oss: SelfCheck.OSS) => oss.exclude)
            .map((oss: SelfCheck.OSS) => oss.gridId)
        );
      },
      onFinish: () => setLoading(false)
    }
  );

  useEffect(() => {
    setTimeout(() => {
      setFiles(
        Array.from(Array(2)).map((_, idx) => ({
          name: `uploaded-oss-list-${idx + 1}.zip`,
          when: '2023-09-28 16:17:30'
        }))
      );
    }, 500);

    loadOssListRequest.execute({});
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
              setEditValues(undefined);
              setIsOSSModalShown(true);
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
            const { keyword, path, copyright, url } = filterParams;

            // Filters
            setFilters({ keyword, path, copyright, url });
            setCurrentPage(1);

            // Sorting
            setSort(filterParams.sort);
          }}
        />
        {paginatedOssList.length > 0 ? (
          <div className="grid grid-cols-1 gap-2 max-h-[700px] p-4 overflow-y-auto no-scrollbar md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {paginatedOssList.map((oss) => (
              <div
                key={oss.gridId}
                className={clsx(
                  'flex flex-col gap-y-1 p-4 border border-gray rounded',
                  excludeList.includes(oss.gridId) && 'bg-semigray/50'
                )}
              >
                <div className="flex justify-between items-center pb-2 mb-1 border-b border-b-darkgray">
                  <input className="w-4 h-4" type="checkbox" />
                  <label className="flex items-center gap-x-2">
                    <span
                      className={clsx(
                        'text-sm',
                        excludeList.includes(oss.gridId) ? 'text-crimson' : 'text-darkgray'
                      )}
                    >
                      exclude
                    </span>
                    <Toogle
                      icons={false}
                      checked={excludeList.includes(oss.gridId)}
                      onChange={() => {
                        if (excludeList.includes(oss.gridId)) {
                          setExcludeList(excludeList.filter((gridId) => gridId !== oss.gridId));
                        } else {
                          setExcludeList([...excludeList, oss.gridId]);
                        }
                      }}
                    />
                  </label>
                </div>
                <div className="flex items-center gap-x-2">
                  <div
                    className={clsx('flex gap-x-1 font-semibold', oss.ossId && 'cursor-pointer')}
                    onClick={() => {
                      if (!oss.ossId) {
                        return;
                      }

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
                  {oss.vuln && (
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
                {oss.licenses.length > 0 && (
                  <div className="line-clamp-3 text-sm text-semiblack/80">
                    {oss.licenses.map((license, idx) => (
                      <span key={idx}>
                        <span
                          className={clsx(license.licenseId && 'cursor-pointer')}
                          onClick={() => {
                            if (!license.licenseId) {
                              return;
                            }

                            const urlQueryParams = new URLSearchParams(queryParams);
                            urlQueryParams.set('modal-type', 'license');
                            urlQueryParams.set('modal-id', license.licenseId);
                            router.push(`${pathname}?${urlQueryParams.toString()}`, {
                              scroll: false
                            });
                          }}
                          dangerouslySetInnerHTML={{
                            __html: highlight(license.licenseName, filters.keyword)
                          }}
                        />
                        {idx < oss.licenses.length - 1 && ', '}
                      </span>
                    ))}
                  </div>
                )}
                <div className="text-sm text-semiblack/80">
                  <i className="fa-regular fa-folder-open" />
                  &ensp;
                  <span
                    dangerouslySetInnerHTML={{
                      __html: highlight(oss.path, filters.path)
                    }}
                  />
                </div>
                <div className="flex items-center gap-x-2 text-sm">
                  {oss.userGuide && (
                    <i
                      className="text-charcoal cursor-pointer fa-solid fa-circle-info"
                      onClick={() => {
                        setModalTitle('User Guide');
                        setModalText(oss.userGuide.split('<br>').join('\n'));
                        setIsTextModalShown(true);
                      }}
                    />
                  )}
                  {oss.copyright && (
                    <i
                      className="text-charcoal cursor-pointer fa-solid fa-copyright"
                      onClick={() => {
                        setModalTitle('Copyright');
                        setModalText(oss.copyright);
                        setIsTextModalShown(true);
                      }}
                    />
                  )}
                  {oss.restrictions && (
                    <i
                      className="text-crimson cursor-pointer fa-solid fa-registered"
                      onClick={() => {
                        setModalTitle('Restrictions');
                        setModalText(oss.restrictions);
                        setIsTextModalShown(true);
                      }}
                    />
                  )}
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
                        setEditValues({
                          ossName: oss.ossName,
                          ossVersion: oss.ossVersion,
                          licenses: oss.licenses,
                          path: oss.path,
                          copyright: oss.copyright,
                          downloadUrl: oss.downloadUrl,
                          homepageUrl: oss.homepageUrl
                        });
                        setIsOSSModalShown(true);
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

      {/* Modals */}
      <SelfCheckOSSModal
        show={isOSSModalShown}
        onHide={() => setIsOSSModalShown(false)}
        values={editValues}
      />
      <Modal show={isTextModalShown} onHide={() => setIsTextModalShown(false)} size="sm">
        <div className="pb-4 mb-4 border-b border-b-semigray font-bold">{modalTitle}</div>
        <div className="whitespace-pre-line">{modalText}</div>
      </Modal>
    </>
  );
}
