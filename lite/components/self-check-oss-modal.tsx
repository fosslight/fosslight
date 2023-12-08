import { useEffect, useState } from 'react';
import Modal from './modal';

export default function SelfCheckOSSModal({
  mode,
  data,
  show,
  onHide
}: {
  mode: 'add' | 'edit';
  data?: SelfCheck.SetOSS;
  show: boolean;
  onHide: () => void;
}) {
  const [licenses, setLicenses] = useState<SelfCheck.OSSLicense[]>([]);

  useEffect(() => {
    if (show) {
      setLicenses(data ? data.licenses : []);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  return (
    <Modal show={show} onHide={onHide} size="lg" hideByBackdrop={false}>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        {mode === 'add' ? (
          <i className="text-sm fa-solid fa-plus" />
        ) : (
          <i className="text-sm fa-solid fa-pen" />
        )}
        &ensp;
        {mode === 'add' ? 'Add an OSS to the list' : 'Edit the OSS'}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2 flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Binary Name or Source Path</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) fosslight, foo/bar/fosslight"
            defaultValue={data ? data.path : ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Name</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) fosslight"
            defaultValue={data ? data.ossName : ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Version</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) 1.0.0"
            defaultValue={data ? data.ossVersion : ''}
          />
        </div>
        <div className="col-span-2 flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Licenses</label>
          <input className="w-full px-2 py-1 border border-darkgray outline-none" defaultValue="" />
          {licenses.length > 0 && (
            <div className="flex gap-x-1">
              {licenses.map((license) => (
                <div
                  key={license.licenseId}
                  className="px-1.5 py-0.5 border border-semiblack/80 rounded text-sm text-semiblack/80"
                >
                  {license.licenseIdentifier}
                  <i
                    className="px-0.5 ml-1.5 text-xs text-crimson cursor-pointer fa-solid fa-x"
                    onClick={() =>
                      setLicenses(licenses.filter((l) => l.licenseId !== license.licenseId))
                    }
                  />
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Download URL</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            defaultValue={data ? data.downloadUrl : ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Homepage URL</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            defaultValue={data ? data.homepageUrl : ''}
          />
        </div>
        <div className="col-span-2 flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Copyright</label>
          <textarea
            className="w-full px-2 py-1 border border-darkgray outline-none resize-none"
            rows={3}
            defaultValue={data ? data.copyright : ''}
          />
        </div>
      </div>
      <div className="flex justify-end gap-x-1 mt-4">
        <button className="px-2 py-0.5 crimson-btn">{mode === 'add' ? 'Add' : 'Edit'}</button>
        <button className="px-2 py-0.5 default-btn" onClick={onHide}>
          Cancel
        </button>
      </div>
    </Modal>
  );
}
