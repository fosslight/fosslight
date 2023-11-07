'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import Loading from './loading';
import Modal from './modal';

export default function DetailModal() {
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
        JSON.stringify(data)
      )}
    </Modal>
  );
}
