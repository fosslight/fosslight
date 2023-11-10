import { ossTypes } from '@/lib/literals';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import DetailModalRow from './detail-modal-row';

export default function DetailModalOSS({ data }: { data: any }) {
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

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
              {data.ossName} ({data.ossVersion})
              <details className="px-2.5 py-1.5 border border-semigray rounded text-sm">
                <summary className="outline-none cursor-pointer no-tap-highlight">
                  Nicknames
                </summary>
                <div className="mt-1">
                  {(data.ossNicknames as string[]).map((ossNickname, idx) => (
                    <div key={idx} className="flex gap-x-1.5 items-center">
                      <span className="px-1 bg-semiblack rounded text-xs text-semiwhite">N</span>
                      {ossNickname}
                    </div>
                  ))}
                </div>
              </details>
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
              {(data.ossType as string).split('').map((x) => (
                <details key={x} open>
                  <summary className="outline-none font-semibold cursor-pointer no-tap-highlight">
                    {ossTypes[x].name}
                  </summary>
                  <div className="mt-0.5 text-sm text-semiblack/80">{ossTypes[x].desc}</div>
                </details>
              ))}
            </div>
          }
          bottomBorder
        />
        <DetailModalRow
          label="Licenses"
          value={(() => {
            const licenseType = (
              <div className="self-start px-1.5 py-0.5 border border-semiblack/80 rounded text-sm text-semiblack/80">
                {data.licenseType}
              </div>
            );

            const options: OSSLicense[][] = [[]];
            (data.licenses as OSSLicense[]).forEach((license) => {
              if (license.comb === 'OR') {
                options.push([]);
              }
              options[options.length - 1].push(license);
            });

            function renderOption(option: OSSLicense[]) {
              return option.map((license, licenseIdx) => (
                <div key={licenseIdx} className="flex gap-x-1 items-center">
                  <div className="line-clamp-1 break-all">◦ {license.name}</div>
                  <div className="flex-shrink-0">({license.identifier})</div>
                  <i
                    className="flex-shrink-0 cursor-pointer fa-solid fa-square-up-right"
                    onClick={() => {
                      const urlQueryParams = new URLSearchParams(queryParams);
                      urlQueryParams.set('modal-type', 'license');
                      urlQueryParams.set('modal-id', license.id);
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
                  <div className="flex gap-x-2 items-center">
                    <i className="text-sm fa-solid fa-file-lines" title="Notice" />
                    <span className="font-semibold text-crimson">
                      You must notify(generate notice).
                    </span>
                  </div>
                )}
                {source && (
                  <div className="flex gap-x-2 items-center">
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
            <a className="text-blue-500 hover:underline" href={data.downloadUrl} target="_blank">
              {data.downloadUrl}
            </a>
          }
        />
        <DetailModalRow
          label="Homepage URL"
          value={
            <a className="text-blue-500 hover:underline" href={data.homepageUrl} target="_blank">
              {data.homepageUrl}
            </a>
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
                  {(data.vulnerabilities as OSSVuln[]).map((vulnerability) => (
                    <tr
                      key={vulnerability.id}
                      className="border-b border-semigray cursor-pointer"
                      onClick={() => {
                        const urlQueryParams = new URLSearchParams(queryParams);
                        urlQueryParams.set('modal-type', 'vuln');
                        urlQueryParams.set('modal-id', vulnerability.id);
                        router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
                      }}
                    >
                      <td className="p-1">{vulnerability.id}</td>
                      <td className="p-1">
                        <a
                          className="text-crimson hover:underline"
                          href={`https://nvd.nist.gov/vuln/detail/${vulnerability.id}`}
                          target="_blank"
                          onClick={(e) => e.stopPropagation()}
                        >
                          {vulnerability.score}
                        </a>
                      </td>
                      <td className="p-1">
                        <div className="whitespace-pre-line">{vulnerability.summary}</div>
                      </td>
                    </tr>
                  ))}
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
