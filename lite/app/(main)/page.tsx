'use client';

import ListSections from '@/components/list-sections';
import { loadingState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import { useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

export default function Dashboard() {
  const setLoading = useSetRecoilState(loadingState);
  const [vulnerabilityList, setVulnerabilityList] = useState<ListSection.Vuln[]>([]);
  const [ossList, setOssList] = useState<ListSection.OSS[]>([]);
  const [licenseList, setLicenseList] = useState<ListSection.License[]>([]);

  // API for loading dashboard data
  const loadDashboardRequest = useAPI('get', '/api/lite/dashboard', {
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
    loadDashboardRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="breadcrumb">Dashboard</h2>

      {/* Description */}
      <h3 className="pb-8">Insights on vulnerabilities, OSS, and licenses.</h3>

      {/* Vulnerabilities, OSS, licenses */}
      <h4 className="shadow-box-header">Recently Registered in Database</h4>
      <ListSections
        vulnerabilityList={vulnerabilityList}
        ossList={ossList}
        licenseList={licenseList}
      />
    </>
  );
}
