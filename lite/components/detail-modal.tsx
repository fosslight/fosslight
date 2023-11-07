'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import DetailModalLicense from './detail-modal-license';
import DetailModalOSS from './detail-modal-oss';
import DetailModalVuln from './detail-modal-vuln';
import Loading from './loading';
import Modal from './modal';

export default function DetailModal() {
  const [dataType, setDataType] = useState<string>('');
  const [data, setData] = useState<any>(null);
  const router = useRouter();
  const queryParams = useSearchParams();
  const modalType = queryParams.get('modal-type');
  const modalId = queryParams.get('modal-id');

  // Load new data based on query parameter information
  useEffect(() => {
    if (!modalType || !modalId) {
      return;
    }

    setDataType(modalType);
    setData(null);

    setTimeout(() => {
      setData({ modalType, modalId });
    }, 500);
  }, [modalType, modalId]);

  return (
    <Modal show={Boolean(modalType && modalId)} onHide={() => router.back()} size="lg">
      {!data ? (
        <div className="flex justify-center py-6">
          <Loading />
        </div>
      ) : (
        (() => {
          if (dataType === 'oss') return <DetailModalOSS data={data} />;
          if (dataType === 'license') return <DetailModalLicense data={data} />;
          if (dataType === 'vuln') return <DetailModalVuln data={data} />;
          return null;
        })()
      )}
    </Modal>
  );
}
