'use client';

import ListSections from '@/components/list-sections';
import { loadingState } from '@/lib/atoms';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

export default function Dashboard() {
  const setLoading = useSetRecoilState(loadingState);
  const [vulnerabilityList, setVulnerabilityList] = useState<any[]>([]);
  const [ossList, setOssList] = useState<any[]>([]);
  const [licenseList, setLicenseList] = useState<any[]>([]);
  const sectionHeaderClass = 'pl-2 mb-4 border-l-4 border-l-semiblack font-bold leading-tight';

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

      setLicenseList(
        Array.from(Array(3)).map(() => ({
          licenseName: 'Apache License 2.0',
          licenseIdentifier: 'Apache-2.0',
          restrictions: ['Non-commercial Use Only', 'Network Copyleft'],
          obligations: 'YY',
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
        Insights on your projects, and recently registered vulnerabilities, oss, and licenses.
      </h3>

      {/* Projects */}
      <h4 className={sectionHeaderClass}>Insights on Projects</h4>
      <div className="w-[calc(100%-4px)] p-4 mb-8 border border-darkgray rounded-lg shadow-[2px_2px_4px_0_rgba(0,0,0,0.2)]">
        ...
      </div>

      {/* Vulnerabilities, oss, licenses */}
      <h4 className={sectionHeaderClass}>Recently Registered in Database</h4>
      <ListSections
        vulnerabilityList={vulnerabilityList}
        ossList={ossList}
        licenseList={licenseList}
      />
    </>
  );
}
