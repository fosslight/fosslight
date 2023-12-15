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

export default function SelfCheckOSS({
  id,
  changed,
  setChanged
}: {
  id: string;
  changed: boolean;
  setChanged: (changed: boolean) => void;
}) {
  const setLoading = useSetRecoilState(loadingState);
  const [method, setMethod] = useState<'file' | 'url'>('file');
  const [fileId, setFileId] = useState('');
  const [fileList, setFileList] = useState<SelfCheck.OSSFile[]>([]);
  const [ossList, setOssList] = useState<SelfCheck.OSS[]>([]);
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

  // API for loading file/OSS list
  const loadDataListRequest = useAPI(
    'get',
    `http://localhost:8180/api/lite/selfchecks/${id}/list-oss`,
    {
      onStart: () => setLoading(true),
      onSuccess: (res) => {
        setFileId('98');
        setFileList([
          {
            fileSeq: '104',
            logiNm: '5ed22fd8-88d1-489d-aa9e-4a398356f30f.xlsx',
            orgNm: 'fosslight_report_all_230907_0155.xlsx',
            created: '2023-12-15 02:02:35',
            deleted: false
          }
        ]);
        setOssList(res.data.list);
      },
      onFinish: () => setLoading(false)
    }
  );

  // API for saving file/OSS List
  const saveOSSRequest = useAPI('post', 'http://localhost:8180/selfCheck/saveSrc', {
    onStart: () => setLoading(true),
    onSuccess: () => {
      alert('Successfully saved files and OSS');
      setChanged(false);
      loadDataListRequest.execute({});
    },
    onFinish: () => setLoading(false),
    type: 'json'
  });

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

  function toggleExclude(gridId: string) {
    const idx = ossList.findLastIndex((oss) => oss.gridId === gridId);
    const oss = ossList[idx];

    setOssList([
      ...ossList.slice(0, idx),
      { ...oss, exclude: !oss.exclude, changed: oss.changed || 'edit' },
      ...ossList.slice(idx + 1)
    ]);
    setChanged(true);
  }

  useEffect(() => {
    loadDataListRequest.execute({});
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
            {fileList.some((file) => !file.deleted) && (
              <div className="flex flex-col gap-y-1 mb-6">
                {fileList
                  .filter((file) => !file.deleted)
                  .map((file, idx) => (
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
                    </a>
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
      <div id="oss-scroll-pos" className="flex justify-between items-center mt-12 mb-2">
        <div className="flex gap-x-3 text-charcoal">
          <i
            className="cursor-pointer fa-solid fa-trash"
            onClick={() => {
              const gridIdsToDelete = Array.from(
                document.querySelectorAll('.oss-to-delete:checked')
              ).map((input) => (input as HTMLInputElement).value);

              if (gridIdsToDelete.length === 0) {
                alert('Check OSS to delete');
                return;
              }

              setOssList(ossList.filter((oss) => !gridIdsToDelete.includes(oss.gridId)));
              setChanged(true);
            }}
          />
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
          <button
            className="px-2 py-0.5 crimson-btn"
            onClick={() => {
              if (!window.confirm('Are you sure to continue?')) {
                return;
              }

              saveOSSRequest.execute({
                body: {
                  prjId: id,
                  mainData: JSON.stringify(
                    ossList.map((oss) => {
                      const obj: any = {
                        gridId: oss.gridId,
                        ossName: oss.ossName,
                        ossVersion: oss.ossVersion,
                        licenseName: oss.licenses.map((license) => license.licenseName).join(','),
                        filePath: oss.path,
                        copyrightText: oss.copyright,
                        downloadLocation: oss.downloadUrl,
                        homepage: oss.homepageUrl,
                        excludeYn: oss.exclude ? 'Y' : 'N'
                      };

                      if (oss.changed !== 'add') {
                        const [referenceId, referenceDiv, componentIdx] = oss.gridId.split('-');

                        obj.componentId = oss.gridId;
                        obj.referenceId = referenceId;
                        obj.referenceDiv = referenceDiv;
                        obj.componentIdx = componentIdx;
                      }

                      return obj;
                    })
                  )
                }
              });
            }}
          >
            Save
          </button>
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
        {changed && <div className="pl-4 text-crimson">* You should save the changes</div>}
        {paginatedOssList.length > 0 ? (
          <div className="grid grid-cols-1 gap-2 max-h-[700px] p-4 overflow-y-auto no-scrollbar md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {paginatedOssList.map((oss) => (
              <div
                key={oss.gridId}
                className={clsx(
                  'flex flex-col gap-y-1 p-4 border rounded',
                  oss.exclude && 'bg-semigray/50',
                  // eslint-disable-next-line no-nested-ternary
                  oss.changed
                    ? oss.changed === 'add'
                      ? 'border-green-500'
                      : 'border-orange-500'
                    : 'border-gray'
                )}
              >
                <div className="flex justify-between items-center pb-2 mb-1 border-b border-b-darkgray">
                  <input className="oss-to-delete w-4 h-4" type="checkbox" value={oss.gridId} />
                  <label className="flex items-center gap-x-2">
                    <span
                      className={clsx('text-sm', oss.exclude ? 'text-crimson' : 'text-darkgray')}
                    >
                      exclude
                    </span>
                    <Toogle
                      icons={false}
                      checked={oss.exclude}
                      onChange={() => toggleExclude(oss.gridId)}
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
                  {oss.path ? (
                    <span
                      dangerouslySetInnerHTML={{
                        __html: highlight(oss.path, filters.path)
                      }}
                    />
                  ) : (
                    <span className="text-darkgray">no path set</span>
                  )}
                </div>
                <div className="flex items-center gap-x-2 text-sm">
                  {oss.userGuide && (
                    <i
                      className="text-charcoal cursor-pointer fa-solid fa-circle-info"
                      title="User Guide"
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
                      title="Copyright"
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
                      title="Restrictions"
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
                          gridId: oss.gridId,
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
        ossList={ossList}
        setOssList={setOssList}
        setChanged={setChanged}
      />
      <Modal show={isTextModalShown} onHide={() => setIsTextModalShown(false)} size="md">
        <div className="pb-4 mb-4 border-b border-b-semigray font-bold">{modalTitle}</div>
        <div className="text-sm whitespace-pre-line">{modalText}</div>
      </Modal>
    </>
  );
}
