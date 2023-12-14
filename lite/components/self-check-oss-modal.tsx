import { useEffect, useState } from 'react';
import Modal from './modal';

export default function SelfCheckOSSModal({
  show,
  onHide,
  values
}: {
  show: boolean;
  onHide: () => void;
  values?: SelfCheck.EditOSS;
}) {
  const [licenses, setLicenses] = useState<SelfCheck.OSSLicense[]>([]);

  useEffect(() => {
    if (show) {
      setLicenses(values?.licenses || []);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  return (
    <Modal show={show} onHide={onHide} size="lg" hideByBackdrop={false}>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        {!values ? (
          <i className="text-sm fa-solid fa-plus" />
        ) : (
          <i className="text-sm fa-solid fa-pen" />
        )}
        &ensp;
        {!values ? 'Add an OSS to the list' : 'Edit the OSS'}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Name</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) fosslight"
            defaultValue={values?.ossName || ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Version</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) 1.0.0"
            defaultValue={values?.ossVersion || ''}
          />
        </div>
        <div className="col-span-2 flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Licenses</label>
          <input className="w-full px-2 py-1 border border-darkgray outline-none" defaultValue="" />
          {licenses.length > 0 && (
            <div className="flex gap-x-1">
              {licenses.map((license, idx) => (
                <div
                  key={idx}
                  className="px-1.5 py-0.5 border border-semiblack/80 rounded text-sm text-semiblack/80"
                >
                  {license.licenseName}
                  <i
                    className="px-0.5 ml-1.5 text-xs text-crimson cursor-pointer fa-solid fa-x"
                    onClick={() => setLicenses(licenses.filter((_, i) => i !== idx))}
                  />
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="col-span-2 flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Binary Name or Source Path</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) fosslight, foo/bar/fosslight"
            defaultValue={values?.path || ''}
          />
        </div>
        <div className="col-span-2 flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Copyright</label>
          <textarea
            className="w-full px-2 py-1 border border-darkgray outline-none resize-none"
            rows={3}
            defaultValue={values?.copyright || ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Download URL</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            defaultValue={values?.downloadUrl || ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Homepage URL</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            defaultValue={values?.homepageUrl || ''}
          />
        </div>
      </div>
      <div className="flex justify-end gap-x-1 mt-4">
        <button className="px-2 py-0.5 crimson-btn">{!values ? 'Add' : 'Edit'}</button>
        <button className="px-2 py-0.5 default-btn" onClick={onHide}>
          Cancel
        </button>
      </div>
    </Modal>
  );
}
