import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import Modal from './modal';

export default function SelfCheckOSSModal({
  show,
  onHide,
  values,
  ossList,
  setOssList,
  setChanged
}: {
  show: boolean;
  onHide: () => void;
  values?: SelfCheck.EditOSS;
  ossList: SelfCheck.OSS[];
  setOssList: (ossList: SelfCheck.OSS[]) => void;
  setChanged: (changed: boolean) => void;
}) {
  const [licenses, setLicenses] = useState<SelfCheck.OSSLicense[]>([]);
  const { register, handleSubmit, reset } = useForm();

  // Mode (add or edit)
  const isAdd = !values;

  useEffect(() => {
    if (show) {
      setLicenses(values?.licenses || []);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  useEffect(() => {
    reset({
      ossName: values?.ossName || '',
      ossVersion: values?.ossVersion || '',
      path: values?.path || '',
      copyright: values?.copyright || '',
      downloadUrl: values?.downloadUrl || '',
      homepageUrl: values?.homepageUrl || ''
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [values]);

  return (
    <Modal show={show} onHide={onHide} size="lg" hideByBackdrop={false}>
      <form
        onSubmit={handleSubmit((data: any) => {
          const formData = { ...data, licenses };

          // Add
          if (isAdd) {
            setOssList([
              ...ossList,
              {
                gridId: `jqg${ossList.filter((oss) => oss.gridId.startsWith('jqg')).length + 1}`,
                ossId: null,
                ossName: formData.ossName,
                ossVersion: formData.ossVersion,
                obligations: [],
                vuln: false,
                cveId: '',
                cvssScore: '',
                licenses: formData.licenses,
                path: formData.path,
                userGuide: '',
                copyright: formData.copyright,
                restrictions: '',
                downloadUrl: formData.downloadUrl,
                homepageUrl: formData.homepageUrl,
                exclude: false,
                changed: 'add'
              }
            ]);
          }

          // Edit
          else {
            const idx = ossList.findLastIndex((oss) => oss.gridId === values.gridId);
            const oss = ossList[idx];

            setOssList([
              ...ossList.slice(0, idx),
              { ...oss, ...formData, changed: oss.changed || 'edit' },
              ...ossList.slice(idx + 1)
            ]);
          }

          setChanged(true);
          onHide();
        })}
      >
        <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
          {isAdd ? (
            <i className="text-sm fa-solid fa-plus" />
          ) : (
            <i className="text-sm fa-solid fa-pen" />
          )}
          &ensp;
          {isAdd ? 'Add an OSS to the list' : 'Edit the OSS'}
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Name</label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              placeholder="EX) fosslight"
              {...register('ossName')}
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Version</label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              placeholder="EX) 1.0.0"
              {...register('ossVersion')}
            />
          </div>
          <div className="col-span-2 flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Licenses</label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              onKeyDown={(e) => {
                const input = e.target as HTMLInputElement;

                if (e.code === 'Enter') {
                  e.preventDefault();

                  if (input.value) {
                    setLicenses([...licenses, { licenseId: null, licenseName: input.value }]);
                    input.value = '';
                  }
                }
              }}
              onBlur={(e) => {
                const input = e.target as HTMLInputElement;

                if (input.value) {
                  setLicenses([...licenses, { licenseId: null, licenseName: input.value }]);
                  input.value = '';
                }
              }}
            />
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
              {...register('path')}
            />
          </div>
          <div className="col-span-2 flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Copyright</label>
            <textarea
              className="w-full px-2 py-1 border border-darkgray outline-none resize-none"
              rows={3}
              {...register('copyright')}
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Download URL</label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              {...register('downloadUrl')}
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Homepage URL</label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              {...register('homepageUrl')}
            />
          </div>
        </div>
        <div className="flex justify-end gap-x-1 mt-4">
          <button className="px-2 py-0.5 crimson-btn">{isAdd ? 'Add' : 'Edit'}</button>
          <button className="px-2 py-0.5 default-btn" type="button" onClick={onHide}>
            Cancel
          </button>
        </div>
      </form>
    </Modal>
  );
}
