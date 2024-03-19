import { loadingState } from '@/lib/atoms';
import { highlight } from '@/lib/commons';
import { useAPI } from '@/lib/hooks';
import { serverOrigin } from '@/lib/literals';
import ExcelIcon from '@/public/images/excel.png';
import clsx from 'clsx';
import dayjs from 'dayjs';
import Image from 'next/image';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useRef, useState } from 'react';
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
  const [fileId, setFileId] = useState('');
  const [fileList, setFileList] = useState<SelfCheck.OSSFile[]>([]);
  const [ossList, setOssList] = useState<SelfCheck.OSS[]>([]);
  const [validMap, setValidMap] = useState<SelfCheck.OSSValidMap>({});
  const checkRef = useRef<Window>();
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Modals
  const [editValues, setEditValues] = useState<SelfCheck.EditOSS>();
  const [isOSSModalShown, setIsOSSModalShown] = useState(false);
  const [fileInfo, setFileInfo] = useState<{ file: SelfCheck.OSSFile; sheets: string[] }>();
  const [isFileModalShown, setIsFileModalShown] = useState(false);
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

  // API for loading validation result
  const loadValidationListRequest = useAPI('get', `/selfCheck/ossGrid/${id}/10`, {
    onSuccess: (res) => {
      const { validData } = res.data;
      const map: SelfCheck.OSSValidMap = {};

      if (validData) {
        const entries = Object.entries(validData) as [string, string][];

        entries.forEach(([key, value]) => {
          const [field, gridId] = key.split('.');

          if (!field || !gridId) {
            return;
          }

          if (!map[gridId]) {
            map[gridId] = {};
          }

          map[gridId][field] = value;
        });
      }

      setValidMap(map);
    }
  });

  // API for loading file/OSS list
  const loadDataListRequest = useAPI('get', `/api/lite/selfchecks/${id}/list-oss`, {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      setFileId(res.data.fileId);
      setFileList(res.data.files);
      setOssList(res.data.oss);

      // Load validation result
      loadValidationListRequest.execute({ params: { referenceId: id } });
    },
    onFinish: () => setLoading(false)
  });

  // API for uploading file
  const uploadFileRequest = useAPI('post', '/project/csvFile', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      const result = JSON.parse(res.data);

      if (result[0][0] && result[1] && result[1].length > 0) {
        document.querySelectorAll('.sheet-checkbox:checked').forEach((cb: any) => {
          // eslint-disable-next-line no-param-reassign
          cb.checked = false;
        });

        const { registFileId, registSeq, fileName, originalFilename, createdDate } = result[0][0];

        setFileInfo({
          file: {
            fileId: registFileId,
            fileSeq: registSeq,
            logiNm: fileName,
            orgNm: originalFilename,
            created: createdDate
          },
          sheets: result[1].map((sheet: any) => sheet.name)
        });
        setIsFileModalShown(true);
      }
    },
    onFinish: () => setLoading(false),
    type: 'file'
  });

  // API for loading sheets
  const loadSheetsRequest = useAPI('post', '/project/getSheetData', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      const loadedOssList = res.data.resultData.mainData;

      setOssList([
        ...ossList,
        ...loadedOssList.map((oss: any) => ({
          gridId: oss.gridId,
          ossId: null,
          ossName: oss.ossName,
          ossVersion: oss.ossVersion,
          obligations: [],
          vuln: false,
          cveId: '',
          cvssScore: '',
          licenses: oss.componentLicenseList.map((license: any) => ({
            licenseId: null,
            licenseName: license.licenseName
          })),
          path: oss.filePath,
          userGuide: '',
          copyright: oss.copyrightText,
          restrictions: '',
          downloadUrl: oss.downloadLocation,
          homepageUrl: oss.homepage,
          exclude: oss.excludeYn === 'Y',
          changed: 'add'
        }))
      ]);
      setChanged(true);
    },
    onFinish: () => setLoading(false),
    type: 'json'
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

  // API for saving file/OSS List
  const saveOSSRequest = useAPI('post', '/selfCheck/saveSrc', {
    onStart: () => setLoading(true),
    onSuccess: () => {
      alert('Successfully saved files and OSS');

      loadDataListRequest.execute({});
      setChanged(false);
    },
    onFinish: () => setLoading(false),
    type: 'json'
  });

  function loadSheets() {
    if (!fileInfo) {
      return;
    }

    const fileSeq = String(fileInfo.file.fileSeq);
    const selectedSheetNums = Array.from(document.querySelectorAll('.sheet-checkbox:checked')).map(
      (cb: any) => cb.value
    );

    if (selectedSheetNums.length === 0) {
      alert('Select sheets to load');
      return;
    }

    if (!fileId) {
      setFileId(fileInfo.file.fileId);
    }
    setFileList([...fileList, { ...fileInfo.file, state: 'add' }]);
    setIsFileModalShown(false);

    loadSheetsRequest.execute({
      body: { prjId: id, fileSeq, sheetNums: selectedSheetNums, readType: 'self' }
    });
  }

  function deleteFile(seq: string) {
    const idx = fileList.findLastIndex((f) => f.fileSeq === seq);
    const file = fileList[idx];

    if (file.state === 'delete') {
      return;
    }

    if (file.state === 'add') {
      setFileList([...fileList.slice(0, idx), ...fileList.slice(idx + 1)]);
    } else {
      setFileList([
        ...fileList.slice(0, idx),
        { ...file, state: 'delete' },
        ...fileList.slice(idx + 1)
      ]);
    }
  }

  function openSubwindowForCheck() {
    const [width, height] = [900, 600];
    const left = window.innerWidth / 2 - width / 2;
    const top = window.innerHeight / 2 - height / 2;

    checkRef.current = window.open(
      `${pathname}/check?`,
      'selfCheckDetailCheck',
      `width=${width}, height=${height}, left=${left}, top=${top}`
    ) as Window;
  }

  function checkAll() {
    const checkedCnt = document.querySelectorAll('.oss-to-delete:checked').length;

    // Check all
    if (paginatedOssList.length > checkedCnt) {
      document.querySelectorAll('.oss-to-delete:not(:checked)').forEach((cb: any) => {
        // eslint-disable-next-line no-param-reassign
        cb.checked = true;
      });
    }

    // Uncheck all
    else {
      document.querySelectorAll('.oss-to-delete:checked').forEach((cb: any) => {
        // eslint-disable-next-line no-param-reassign
        cb.checked = false;
      });
    }
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

  useEffect(() => {
    function handlePostMessage(e: MessageEvent<any>) {
      if (e.source === checkRef.current) {
        const data = JSON.parse(e.data);
        let newOssList: SelfCheck.OSS[];

        // Change OSS names
        if (data.ossCheck) {
          newOssList = ossList.map((oss) => {
            if (oss.gridId in data.ossCheck) {
              const newOssName = data.ossCheck[oss.gridId];
              return { ...oss, ossName: newOssName, changed: oss.changed || 'edit' };
            }
            return oss;
          });
        }

        // Change Licenses
        else {
          newOssList = ossList.map((oss) => {
            if (oss.gridId in data.licenseCheck) {
              const newLicenses = data.licenseCheck[oss.gridId];
              return {
                ...oss,
                licenses: newLicenses.map((license: string) => ({
                  licenseId: null,
                  licenseName: license
                })),
                changed: oss.changed || 'edit'
              };
            }
            return oss;
          });
        }

        setOssList(newOssList);
        setChanged(true);
      }
    }

    window.addEventListener('message', handlePostMessage);
    return () => {
      window.removeEventListener('message', handlePostMessage);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ossList]);

  return (
    <>
      {/* Uploading files */}
      <div className="p-4 mb-12 border border-dashed border-semigray rounded text-center">
        {fileList.some((file) => file.state !== 'delete') && (
          <div className="flex flex-col items-center gap-y-1 mb-6">
            {fileList
              .filter((file) => file.state !== 'delete')
              .map((file, idx) => (
                <a
                  key={idx}
                  className="flex justify-center items-center gap-x-1.5 text-sm"
                  href={`${serverOrigin}/download/${file.fileSeq}/${file.logiNm}`}
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
                      deleteFile(file.fileSeq);
                    }}
                  />
                </a>
              ))}
          </div>
        )}
        <div className="relative">
          <i className="fa-solid fa-arrow-up-from-bracket" />
          &ensp;Upload a source analysis result file here
          <div className="mt-1 text-sm text-darkgray">
            (The file must contain information of OSS to be listed.)
          </div>
          <input
            id="upload-file"
            className="absolute inset-0 opacity-0 cursor-pointer"
            type="file"
            accept=".xlsx, .xls, .xlsm, .csv"
            onChange={(e) => {
              const input = e.target;

              if (!input.files || input.files.length === 0) {
                return;
              }

              const file = input.files[0];
              const allowedExtensions = /\.(xlsx|xls|xlsm|csv)$/i;
              if (!allowedExtensions.test(file.name)) {
                alert('Select a file with valid extension(xlsx, xls, xlsm, or csv)');
                input.value = '';
                return;
              }

              const formData = new FormData();
              formData.append('myfile', file, file.name);
              formData.append('registFileId', fileId);
              formData.append('tabNm', 'SELF');

              uploadFileRequest.execute({ body: formData });
              input.value = '';
            }}
          />
        </div>
      </div>

      {/* Buttons */}
      <div id="oss-scroll-pos" className="flex justify-between items-center mb-2">
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
          <button
            className="flex items-center gap-x-1.5 px-2 py-0.5 default-btn"
            onClick={() => {
              if (changed) {
                alert('You should save first');
                return;
              }

              downloadReportRequest.execute({ body: { parameter: id, type: 'selfReport' } });
            }}
          >
            <div className="relative w-4 h-4">
              <Image src={ExcelIcon} fill sizes="32px" alt="export" />
            </div>
            Export
          </button>
          <button
            className="px-2 py-0.5 default-btn"
            onClick={() => {
              if (changed) {
                alert('You should save first');
                return;
              }

              openSubwindowForCheck();
            }}
          >
            Check
          </button>
          <button
            className="px-2 py-0.5 crimson-btn"
            onClick={() => {
              if (!window.confirm('Are you sure to continue?')) {
                return;
              }

              const addFileSeqs = fileList
                .filter((file) => file.state === 'add')
                .map((file) => file.fileSeq);

              const delFileSeqs = fileList
                .filter((file) => file.state === 'delete')
                .map((file) => file.fileSeq);

              saveOSSRequest.execute({
                body: {
                  prjId: id,

                  // Files
                  csvFileId: addFileSeqs.length > 0 ? fileId : '',
                  csvFileSeqs: JSON.stringify(addFileSeqs.map((seq) => ({ fileSeq: Number(seq) }))),
                  csvDelFileIds: JSON.stringify(delFileSeqs.map((seq) => ({ fileSeq: seq }))),

                  // OSS
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
          {changed && (
            <span
              className="inline-block align-top w-2 h-2 ml-1.5 bg-red-500 rounded-full text-semiwhite"
              title="There are some changes to save"
            />
          )}
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
        <button className="px-2 py-0.5 ml-4 text-sm default-btn" onClick={checkAll}>
          Check all
        </button>
        {paginatedOssList.length > 0 ? (
          <div className="grid grid-cols-1 gap-2 p-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
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
                    {oss.ossName ? (
                      <div className="line-clamp-1 break-all">
                        <span
                          dangerouslySetInnerHTML={{
                            __html: highlight(oss.ossName, filters.keyword)
                          }}
                        />
                      </div>
                    ) : (
                      <span className="text-red-500">OSS required</span>
                    )}
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
                  {(() => {
                    if (
                      oss.changed ||
                      !validMap[oss.gridId] ||
                      (!validMap[oss.gridId].ossName && !validMap[oss.gridId].ossVersion)
                    ) {
                      return null;
                    }

                    const errors = [];
                    if (validMap[oss.gridId].ossName) {
                      errors.push(`Name: ${validMap[oss.gridId].ossName}`);
                    }
                    if (validMap[oss.gridId].ossVersion) {
                      errors.push(`Version: ${validMap[oss.gridId].ossVersion}`);
                    }

                    return (
                      <i
                        className="text-red-500 fa-solid fa-triangle-exclamation"
                        title={errors.join('\n')}
                      />
                    );
                  })()}
                </div>
                <div className="flex text-sm">
                  <div
                    className="text-semiblack/80 line-clamp-1 break-all"
                    title={
                      oss.licenses.length > 0
                        ? oss.licenses.map((license) => license.licenseName).join(', ')
                        : undefined
                    }
                  >
                    {oss.licenses.length > 0 ? (
                      oss.licenses.map((license, idx) => (
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
                      ))
                    ) : (
                      <span className="text-red-500">License required</span>
                    )}
                  </div>
                  {(() => {
                    if (oss.changed || !validMap[oss.gridId] || !validMap[oss.gridId].licenseName) {
                      return null;
                    }

                    return (
                      <i
                        className="flex-shrink-0 pt-1 pl-1.5 text-red-500 fa-solid fa-triangle-exclamation"
                        title={`License: ${validMap[oss.gridId].licenseName}`}
                      />
                    );
                  })()}
                </div>
                <div className="text-sm text-semiblack/80">
                  <i className="fa-regular fa-folder-open" />
                  &ensp;
                  {oss.path ? (
                    <span
                      className="break-all"
                      dangerouslySetInnerHTML={{
                        __html: highlight(oss.path, filters.path)
                      }}
                    />
                  ) : (
                    <span className="text-darkgray">Path not set</span>
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
      <Modal show={isFileModalShown} onHide={() => setIsFileModalShown(false)} size="sm">
        <div className="pb-4 mb-4 border-b border-b-semigray font-bold">Select sheets</div>
        <div className="flex flex-col items-start gap-y-1 text-sm">
          {fileInfo?.sheets.map((sheet, idx) => (
            <label key={idx} className="flex items-center cursor-pointer hover:opacity-70">
              <input className="sheet-checkbox" type="checkbox" value={idx} />
              &ensp;
              {sheet}
            </label>
          ))}
        </div>
        <div className="flex justify-end gap-x-1 mt-4">
          <button className="px-2 py-0.5 crimson-btn" onClick={loadSheets}>
            Load
          </button>
          <button className="px-2 py-0.5 default-btn" onClick={() => setIsFileModalShown(false)}>
            Cancel
          </button>
        </div>
      </Modal>
      <Modal show={isTextModalShown} onHide={() => setIsTextModalShown(false)} size="md">
        <div className="pb-4 mb-4 border-b border-b-semigray font-bold">{modalTitle}</div>
        <div className="text-sm whitespace-pre-line">{modalText}</div>
      </Modal>
    </>
  );
}
