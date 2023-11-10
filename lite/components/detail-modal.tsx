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
      if (modalType === 'oss') {
        setData({
          ossName: 'cairo',
          ossNicknames: ['Cairo Vector Graphics', 'Cairo Vector Graphics Library'],
          ossVersion: '1.4.12',
          ossType: 'MD',
          licenses: [
            {
              id: '123',
              name: 'Mozilla Public License 1.1',
              identifier: 'MPL-1.1',
              comb: ''
            },
            {
              id: '124',
              name: 'GNU General Public License v2.0 only',
              identifier: 'GPL-2.0',
              comb: 'AND'
            },
            {
              id: '125',
              name: 'GNU Lesser General Public License v2.1 only',
              identifier: 'LGPL-2.1',
              comb: 'OR'
            },
            {
              id: '124',
              name: 'GNU General Public License v2.0 only',
              identifier: 'GPL-2.0',
              comb: 'AND'
            }
          ],
          licenseType: 'Copyleft',
          obligations: 'YY',
          downloadUrl: 'http://cairographics.org/releases',
          homepageUrl: 'https://www.cairographics.org',
          description: 'Some files in util and test folder are released under GPL-2.0',
          copyright: 'Copyright (c) 2013 the PM2 project\nCopyright (c) 2013-present, Keymetrics',
          attribution: 'There some content about attribution here.',
          vulnerabilities: [
            {
              id: 'CVE-2020-35492',
              score: '7.8',
              summary: 'A flaw was found in cairo image-compositor.c.'
            },
            {
              id: 'CVE-2020-35492',
              score: '7.8',
              summary: 'A flaw was found in cairo image-compositor.c.'
            }
          ],
          deactivate: false,
          creator: 'admin',
          created: '2023-10-05 23:54:08.0',
          modifier: 'admin',
          modified: '2023-10-07 21:32:05.0'
        });
      } else if (modalType === 'license') {
        setData({
          licenseName: 'Apache License 2.0',
          licenseNicknames: ['Apache', 'Apaceh 2.0'],
          licenseIdentifier: 'Apache-2.0',
          licenseType: 'Permissive',
          obligations: 'YY',
          restrictions: ['Non-commercial Use Only', 'Network Copyleft'],
          homepageUrl: 'https://spdx.org/licenses/blessing.html',
          description: 'There are some descriptions here.',
          licenseText: 'There are some license texts here.',
          attribution: 'There are some attribution here.',
          creator: 'admin',
          created: '2023-10-05 23:54:08.0',
          modifier: 'admin',
          modified: '2023-10-07 21:32:05.0'
        });
      } else {
        setData({ modalType, modalId });
      }
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
