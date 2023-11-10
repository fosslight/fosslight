import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import DetailModalRow from './detail-modal-row';

export default function DetailModalVuln({ data }: { data: any }) {
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  return (
    <>
      <div className="pb-6 text-lg font-black">
        <i className="text-base fa-solid fa-cube" />
        &ensp;Vulnerability Detail
      </div>
      <div className="flex flex-col gap-y-5 lg:gap-y-3">
        <DetailModalRow
          label="CVE ID"
          value={
            <div className="flex gap-x-1.5 items-center">
              {data.cveId}
              <a href={`https://nvd.nist.gov/vuln/detail/${data.cveId}`} target="_blank">
                <i className="text-crimson fa-solid fa-square-up-right" />
              </a>
            </div>
          }
          bottomBorder
        />
        <DetailModalRow
          label="CVSS Score"
          value={<div className="text-orange-500">{data.cvssScore}</div>}
          bottomBorder
        />
        <DetailModalRow
          label="Summary"
          value={<div className="whitespace-pre-line">{data.summary}</div>}
          bottomBorder
        />
        <DetailModalRow label="Publish" value={data.published.substring(0, 16)} />
        <DetailModalRow label="Modify" value={data.modified.substring(0, 16)} bottomBorder />
        <DetailModalRow
          label="OSS"
          value={
            <div className="overflow-x-auto no-scrollbar">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-semiblack text-semiwhite text-left whitespace-nowrap overflow-hidden">
                    <th className="px-1.5 py-1 rounded-tl font-semibold">ID</th>
                    <th className="px-1.5 py-1 font-semibold">Name (Nickname)</th>
                    <th className="px-1.5 py-1 rounded-tr font-semibold">Ver</th>
                  </tr>
                </thead>
                <tbody>
                  {(data.oss as VulnOSS[]).map((oss) => (
                    <tr
                      key={oss.id}
                      className="border-b border-semigray cursor-pointer"
                      onClick={() => {
                        const urlQueryParams = new URLSearchParams(queryParams);
                        urlQueryParams.set('modal-type', 'oss');
                        urlQueryParams.set('modal-id', oss.id);
                        router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
                      }}
                    >
                      <td className="p-1">{oss.id}</td>
                      <td className="p-1">
                        {oss.name} ({oss.nickname})
                      </td>
                      <td className="p-1">{oss.ver}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          }
        />
      </div>
    </>
  );
}
