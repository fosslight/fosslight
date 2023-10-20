'use client';

import { loadingState } from '@/lib/atoms';
import clsx from 'clsx';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

export default function Dashboard() {
  const setLoading = useSetRecoilState(loadingState);
  const [vulnerabilityList, setVulnerabilityList] = useState<any[]>([]);
  const [ossList, setOssList] = useState<any[]>([]);

  const sectionHeaderClass = 'pl-2 mb-4 border-l-4 border-l-semiblack font-bold leading-tight';
  const sectionClass =
    'p-4 border border-darkgray rounded-lg shadow-[2px_2px_4px_0_rgba(0,0,0,0.2)]';

  // Load recent rows for each section
  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setVulnerabilityList(
        Array.from(Array(3)).map(() => ({
          cveId: 'CVE-2020-35492',
          ossName: 'cairo',
          ossVersion: '1.4.12',
          cvssScore: '7.8',
          summary: 'A flaw was found in cairo image-compositor.c in all versions prior to 1.17.4.',
          published: '2021-03-18 23:54:08.0',
          modified: '2023-05-03 21:32:05.0'
        }))
      );

      setOssList(
        Array.from(Array(3)).map(() => ({
          ossName: 'cairo',
          ossVersion: '1.4.12',
          licenseName: '(MPL-1.1 AND GPL-2.0) OR (LGPL-2.1 AND GPL-2.0)',
          obligations: 'YY',
          cvssScore: '7.8',
          created: '2023-10-05 23:54:08.0',
          modified: '2023-10-07 21:32:05.0'
        }))
      );

      setLoading(false);
    }, 500);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="pb-2 text-xl font-black">Dashboard</h2>

      {/* Description */}
      <h3 className="pb-8">
        Insights on your projects, and recently registered OSSs, licenses, vulnerabilities.
      </h3>

      {/* Recent Projects */}
      <h4 className={sectionHeaderClass}>Insights on Projects</h4>
      <div className={clsx('w-[calc(100%-4px)] mb-8', sectionClass)}>...</div>

      {/* Newly Registered in Database */}
      <h4 className={sectionHeaderClass}>Recently Registered in Database</h4>
      <div className="grid grid-cols-2 gap-4 w-[calc(100%-4px)]">
        <div className={clsx('col-span-2', sectionClass)}>
          <div className="flex items-center gap-x-3 text-sm">
            <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">Vulnerability</div>
            <Link className="text-charcoal" href="/database/vulnerability?s=mod-dsc">
              show more here
            </Link>
          </div>
          <div className="flex flex-col gap-y-3 mt-4">
            {vulnerabilityList.length > 0 ? (
              vulnerabilityList.map((vulnerability, idx) => (
                <div
                  key={idx}
                  className="flex gap-x-3 pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex justify-center items-center flex-shrink-0 w-8 h-8 border border-crimson rounded-full text-crimson">
                    {vulnerability.cvssScore}
                  </div>
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div className="line-clamp-1 font-semibold">{vulnerability.ossName}</div>
                      <div className="flex-shrink-0 font-semibold">
                        ({vulnerability.ossVersion})
                      </div>
                      <div className="flex-shrink-0 px-1 py-0.5 bg-darkgray rounded text-xs text-semiwhite">
                        {vulnerability.cveId}
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">
                      {vulnerability.summary}
                    </div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check"></i>&ensp;
                      {vulnerability.published < vulnerability.modified
                        ? `${vulnerability.modified.substring(0, 10)} modified`
                        : `${vulnerability.published.substring(0, 10)} published`}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-darkgray">No entries</div>
            )}
          </div>
        </div>
        <div className={clsx('col-span-2 lg:col-span-1', sectionClass)}>
          <div className="flex items-center gap-x-3 text-sm">
            <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">OSS</div>
            <Link className="text-charcoal" href="/database/oss?s=mod-dsc">
              show more here
            </Link>
          </div>
          <div className="flex flex-col gap-y-3 mt-4">
            {ossList.length > 0 ? (
              ossList.map((oss, idx) => (
                <div
                  key={idx}
                  className="flex gap-x-3 pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div className="line-clamp-1 font-semibold">{oss.ossName}</div>
                      <div className="flex-shrink-0 font-semibold">({oss.ossVersion})</div>
                      <div className="flex items-center gap-x-1 flex-shrink-0 px-1 py-1 border border-darkgray rounded text-xs">
                        {oss.obligations[0] === 'Y' && (
                          <i className="fa-solid fa-file-lines" title="Notice"></i>
                        )}
                        {oss.obligations[1] === 'Y' && (
                          <i className="fa-solid fa-code" title="Source"></i>
                        )}
                      </div>
                      <div className="flex-shrink-0 px-1 py-0.5 border border-orange-500 rounded text-xs">
                        <span className="text-orange-500">{oss.cvssScore}</span>
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">{oss.licenseName}</div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check"></i>&ensp;
                      {oss.created < oss.modified
                        ? `${oss.modified.substring(0, 10)} modified`
                        : `${oss.created.substring(0, 10)} created`}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-darkgray">No entries</div>
            )}
          </div>
        </div>
        <div className={clsx('col-span-2 lg:col-span-1', sectionClass)}>...</div>
      </div>
    </>
  );
}
