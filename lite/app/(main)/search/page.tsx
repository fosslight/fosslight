'use client';

import ListSections from '@/components/list-sections';
import { loadingState } from '@/lib/atoms';
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

export default function FullSearch() {
  const setLoading = useSetRecoilState(loadingState);
  const [vulnerabilityList, setVulnerabilityList] = useState<any[]>([]);
  const [ossList, setOssList] = useState<any[]>([]);
  const [licenseList, setLicenseList] = useState<any[]>([]);

  const queryParams = useSearchParams();
  const keyword = queryParams.get('keyword') || '';

  // Load recent rows for each section
  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      setVulnerabilityList(
        Array.from(Array(3)).map(() => ({
          ossName: 'cairo',
          ossVersion: '1.4.12',
          cveId: 'CVE-2020-35492',
          cvssScore: '7.8',
          summary: 'A flaw was found in cairo image-compositor.c in all versions prior to 1.17.4.',
          published: '2021-03-18 23:54:08.0',
          modified: '2023-05-03 21:32:05.0'
        }))
      );

      setOssList(
        Array.from(Array(3)).map((_, idx) => ({
          ossId: String(3 - idx),
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
        Array.from(Array(3)).map((_, idx) => ({
          licenseId: String(3 - idx),
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
  }, [keyword]);

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="breadcrumb">Search Result</h2>

      {/* Description */}
      <h3 className="pb-8">
        Search results of vulnerabilities, OSS, and licenses. Each section shows up to 5 search
        results.
      </h3>

      {/* Search results */}
      <ListSections
        vulnerabilityList={vulnerabilityList}
        ossList={ossList}
        licenseList={licenseList}
        searchKeyword={keyword}
      />
    </>
  );
}
