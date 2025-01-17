import { useAPI } from '@/lib/hooks';
import { OSS_TYPES } from '@/lib/literals';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import DetailModalRow from './detail-modal-row';
import Loading from './loading';

export default function DetailModalOSS({ modalId }: { modalId: string }) {
  const [data, setData] = useState<Detail.OSS | null>(null);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // API for loading data
  const loadDataRequest = useAPI('get', `/api/lite/oss/${modalId}`, {
    onSuccess: (res) => {
      setData(res.data.oss);
    },
    onError: () => router.replace('/database/oss')
  });

  // Load data based on query parameter information
  useEffect(() => {
    if (!modalId) {
      return;
    }

    loadDataRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [modalId]);

  if (!data) {
    return (
      <div className="flex justify-center py-6">
        <Loading />
      </div>
    );
  }

  return (
    <>
      <div className="pb-6 text-lg font-black">
        <i className="text-base fa-solid fa-cube" />
        &ensp;OSS Detail
      </div>
      <div className="flex flex-col gap-y-5 lg:gap-y-3">
        <DetailModalRow
          label="Name & Version"
          value={
            <div className="flex flex-col gap-y-3">
              {`${data.ossName}${data.ossVersion ? ` (${data.ossVersion})` : ''}`}
              {data.ossNicknames && (
                <details className="px-2.5 py-1.5 border border-semigray rounded text-sm">
                  <summary className="outline-none cursor-pointer no-tap-highlight">
                    Nicknames
                  </summary>
                  <div className="mt-1">
                    {data.ossNicknames.map((ossNickname, idx) => (
                      <div key={idx} className="flex items-center gap-x-1.5">
                        <span className="px-1 bg-semiblack rounded text-xs text-semiwhite">N</span>
                        {ossNickname}
                      </div>
                    ))}
                  </div>
                </details>
              )}
              {data.deactivate && (
                <div className="text-sm text-crimson">* This is deactivated OSS.</div>
              )}
            </div>
          }
          bottomBorder
        />
        <DetailModalRow
          label="Type"
          value={
            <div className="flex flex-col gap-y-3">
              {(() => {
                const idxToKey = Object.fromEntries(
                  Object.keys(OSS_TYPES).map((key, idx) => [idx, key])
                );

                return data.ossType.split('').map((x, idx) => {
                  if (x !== '1') {
                    return null;
                  }

                  const key = idxToKey[idx];
                  const typeInfo = OSS_TYPES[key];

                  return (
                    <details key={key} open>
                      <summary className="outline-none font-semibold cursor-pointer no-tap-highlight">
                        {typeInfo.name}
                      </summary>
                      <div className="mt-0.5 text-sm text-semiblack/80">{typeInfo.desc}</div>
                    </details>
                  );
                });
              })()}
            </div>
          }
          bottomBorder
        />
        <DetailModalRow
          label="Licenses"
          value={(() => {
            const licenseType = data.licenseType ? (
              <div className="self-start px-1.5 py-0.5 border border-semiblack/80 rounded text-sm text-semiblack/80">
                {data.licenseType}
              </div>
            ) : null;

            const options: Detail.OSSLicense[][] = [[]];
            data.licenses.forEach((license) => {
              if (license.comb === 'OR') {
                options.push([]);
              }
              options[options.length - 1].push(license);
            });

            function renderOption(option: Detail.OSSLicense[]) {
              return option.map((license, licenseIdx) => (
                <div key={licenseIdx} className="flex items-center gap-x-1">
                  <div className="line-clamp-1 break-all">◦ {license.licenseName}</div>
                  <div className="flex-shrink-0">({license.licenseIdentifier})</div>
                  <i
                    className="flex-shrink-0 cursor-pointer fa-solid fa-square-up-right"
                    onClick={() => {
                      const urlQueryParams = new URLSearchParams(queryParams);
                      urlQueryParams.set('modal-type', 'license');
                      urlQueryParams.set('modal-id', license.licenseId);
                      router.push(`${pathname}?${urlQueryParams.toString()}`, {
                        scroll: false
                      });
                    }}
                  />
                </div>
              ));
            }

            if (options.length === 0) {
              return null;
            }

            if (options.length === 1) {
              return (
                <div className="flex flex-col gap-y-3">
                  {licenseType}
                  <div className="text-sm text-semiblack/80">{renderOption(options[0])}</div>
                </div>
              );
            }

            return (
              <div className="flex flex-col gap-y-3">
                {licenseType}
                {options.map((option, idx) => (
                  <details key={idx} open>
                    <summary className="outline-none font-semibold cursor-pointer no-tap-highlight">
                      Option {idx + 1}
                    </summary>
                    <div className="mt-0.5 text-sm text-semiblack/80">{renderOption(option)}</div>
                  </details>
                ))}
                <div className="text-sm text-darkgray">
                  * Select one of the above options. (∵ Dual License)
                </div>
              </div>
            );
          })()}
          bottomBorder
        />
        <DetailModalRow
          label="Obligations"
          value={(() => {
            const notice = data.obligations[0] === 'Y';
            const source = data.obligations[1] === 'Y';

            if (!notice && !source) {
              return null;
            }

            return (
              <div className="flex flex-col gap-y-3">
                {notice && (
                  <div className="flex items-center gap-x-2">
                    <i className="text-sm fa-solid fa-file-lines" title="Notice" />
                    <span className="font-semibold text-crimson">
                      You must notify(generate notice).
                    </span>
                  </div>
                )}
                {source && (
                  <div className="flex items-center gap-x-2">
                    <i className="text-sm fa-solid fa-code" title="Source" />
                    <span className="font-semibold text-crimson">
                      You must disclose the source code.
                    </span>
                  </div>
                )}
              </div>
            );
          })()}
          bottomBorder
        />
        <DetailModalRow
          label="Download URL"
          value={
            data.downloadUrl && (
              <a className="text-blue-500 hover:underline" href={data.downloadUrl} target="_blank">
                {data.downloadUrl}
              </a>
            )
          }
        />
        <DetailModalRow
          label="Homepage URL"
          value={
            data.homepageUrl && (
              <a className="text-blue-500 hover:underline" href={data.homepageUrl} target="_blank">
                {data.homepageUrl}
              </a>
            )
          }
          bottomBorder
        />
        <DetailModalRow
          label="Description"
          value={<div className="whitespace-pre-line">{data.description}</div>}
        />
        <DetailModalRow
          label="Copyright"
          value={<div className="whitespace-pre-line">{data.copyright}</div>}
        />
        <DetailModalRow
          label="Attribution"
          value={<div className="whitespace-pre-line">{data.attribution}</div>}
          bottomBorder
        />
        <DetailModalRow
          label="Vulnerabilities"
          value={
            <div className="overflow-x-auto no-scrollbar">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-crimson text-semiwhite text-left whitespace-nowrap overflow-hidden">
                    <th className="px-1.5 py-1 rounded-tl font-semibold">CVE ID</th>
                    <th className="px-1.5 py-1 font-semibold">Score</th>
                    <th className="px-1.5 py-1 rounded-tr font-semibold">Summary</th>
                  </tr>
                </thead>
                <tbody>
                  {data.vulnerabilities.length > 0 ? (
                    data.vulnerabilities.map((vulnerability) => (
                      <tr
                        key={vulnerability.cveId}
                        className="border-b border-semigray cursor-pointer"
                        onClick={() => {
                          const urlQueryParams = new URLSearchParams(queryParams);
                          urlQueryParams.set('modal-type', 'vuln');
                          urlQueryParams.set('modal-id', vulnerability.cveId);
                          router.push(`${pathname}?${urlQueryParams.toString()}`, {
                            scroll: false
                          });
                        }}
                      >
                        <td className="p-1">{vulnerability.cveId}</td>
                        <td className="p-1">
                          <a
                            className="text-crimson hover:underline"
                            href={`https://nvd.nist.gov/vuln/detail/${vulnerability.cveId}`}
                            target="_blank"
                            onClick={(e) => e.stopPropagation()}
                          >
                            {vulnerability.cvssScore}
                          </a>
                        </td>
                        <td className="p-1">
                          <div className="whitespace-pre-line line-clamp-3">
                            {vulnerability.summary}
                          </div>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr className="border-b border-semigray text-center">
                      <td className="px-1 py-2" colSpan={3}>
                        There are no entries.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          }
          bottomBorder
        />
        <DetailModalRow
          label="Create"
          value={`${data.created.substring(0, 16)} (${data.creator})`}
        />
        <DetailModalRow
          label="Modify"
          value={`${data.modified.substring(0, 16)} (${data.modifier})`}
        />
      </div>
    </>
  );
}
