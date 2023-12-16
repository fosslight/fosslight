'use client';

import ListSections from '@/components/list-sections';
import { loadingState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

export default function FullSearch() {
  const setLoading = useSetRecoilState(loadingState);
  const [vulnerabilityList, setVulnerabilityList] = useState<ListSection.Vuln[]>([]);
  const [ossList, setOssList] = useState<ListSection.OSS[]>([]);
  const [licenseList, setLicenseList] = useState<ListSection.License[]>([]);

  const queryParams = useSearchParams();
  const keyword = queryParams.get('keyword') || '';

  // API for loading search result
  const searchRequest = useAPI('get', 'http://localhost:8180/api/lite/dashboard/search', {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      const { vulnerabilities, oss, licenses } = res.data;
      setVulnerabilityList(vulnerabilities);
      setOssList(oss);
      setLicenseList(licenses);
    },
    onFinish: () => setLoading(false)
  });

  // Load recent rows for each section
  useEffect(() => {
    searchRequest.execute({ params: { query: keyword } });
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
