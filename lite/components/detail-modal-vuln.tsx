import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import DetailModalRow from './detail-modal-row';
import Loading from './loading';

export default function DetailModalVuln({ modalId }: { modalId: string }) {
  const [data, setData] = useState<Detail.Vuln | null>(null);
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  // Load data based on query parameter information
  useEffect(() => {
    if (!modalId) {
      return;
    }

    setData(null);

    setTimeout(() => {
      setData({
        cveId: 'CVE-2020-35492',
        cvssScore: '7.8',
        summary: 'A flaw was found in cairo image-compositor.c in all versions prior to 1.17.4.',
        published: '2021-03-18 23:54:08.0',
        modified: '2023-05-03 21:32:05.0',
        oss: [
          { ossId: '123', ossName: 'cairo', ossVersion: '1.4.12' },
          { ossId: '124', ossName: 'cairo', ossVersion: '1.4.12' }
        ]
      });
    }, 500);
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
        &ensp;Vulnerability Detail
      </div>
      <div className="flex flex-col gap-y-5 lg:gap-y-3">
        <DetailModalRow
          label="CVE ID"
          value={
            <div className="flex items-center gap-x-1.5">
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
          value={<div className="text-crimson">{data.cvssScore}</div>}
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
                    <th className="px-1.5 py-1 font-semibold">Name</th>
                    <th className="px-1.5 py-1 rounded-tr font-semibold">Ver</th>
                  </tr>
                </thead>
                <tbody>
                  {data.oss.map((oss) => (
                    <tr
                      key={oss.ossId}
                      className="border-b border-semigray cursor-pointer"
                      onClick={() => {
                        const urlQueryParams = new URLSearchParams(queryParams);
                        urlQueryParams.set('modal-type', 'oss');
                        urlQueryParams.set('modal-id', oss.ossId);
                        router.push(`${pathname}?${urlQueryParams.toString()}`, { scroll: false });
                      }}
                    >
                      <td className="p-1">{oss.ossId}</td>
                      <td className="p-1">{oss.ossName}</td>
                      <td className="p-1">{oss.ossVersion}</td>
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
