import { useAPI } from '@/lib/hooks';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import DetailModalRow from './detail-modal-row';
import Loading from './loading';

export default function DetailModalVuln({ modalId }: { modalId: string }) {
  const [data, setData] = useState<Detail.Vuln | null>(null);
  const router = useRouter();

  // API for loading data
  const loadDataRequest = useAPI('get', `/api/lite/vulnerabilities/${modalId}`, {
    onSuccess: (res) => {
      setData(res.data.vulnerability);
    },
    onError: () => router.replace('/database/vulnerability')
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
        <DetailModalRow label="Modify" value={data.modified.substring(0, 16)} bottomBorder />
        <DetailModalRow
          label="OSS"
          value={
            <div className="max-h-[300px] overflow-auto no-scrollbar">
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-semiblack text-semiwhite text-left whitespace-nowrap overflow-hidden">
                    <th className="px-1.5 py-1 font-semibold">Name</th>
                    <th className="px-1.5 py-1 rounded-tr font-semibold">Ver</th>
                  </tr>
                </thead>
                <tbody>
                  {data.oss.map((oss, idx) => (
                    <tr key={idx} className={'border-b border-semigray'}>
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
