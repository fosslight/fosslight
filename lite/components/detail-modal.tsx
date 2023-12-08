import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import DetailModalLicense from './detail-modal-license';
import DetailModalOSS from './detail-modal-oss';
import DetailModalVuln from './detail-modal-vuln';
import Modal from './modal';

export default function DetailModal() {
  const [dataType, setDataType] = useState('');
  const router = useRouter();
  const queryParams = useSearchParams();
  const modalType = queryParams.get('modal-type') || '';
  const modalId = queryParams.get('modal-id') || '';

  useEffect(() => {
    if (!modalType || !modalId) {
      return;
    }

    setDataType(modalType);
  }, [modalType, modalId]);

  return (
    <Modal show={Boolean(modalType && modalId)} onHide={() => router.back()} size="lg">
      {(() => {
        if (dataType === 'oss') return <DetailModalOSS modalId={modalId} />;
        if (dataType === 'license') return <DetailModalLicense modalId={modalId} />;
        if (dataType === 'vuln') return <DetailModalVuln modalId={modalId} />;
        return null;
      })()}
    </Modal>
  );
}
