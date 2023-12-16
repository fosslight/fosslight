import { useAPI } from '@/lib/hooks';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import Select from 'react-select/creatable';
import Modal from './modal';

const options = [
  { value: 'chocolate', label: 'Chocolate' },
  { value: 'strawberry', label: 'Strawberry' },
  { value: 'vanilla', label: 'Vanilla' }
];

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
  const { register, handleSubmit, reset, watch, control } = useForm();

  // Autocomplete
  const [autocompleteOss, setAutocompleteOss] = useState<[string, string][]>([]);
  const [autocompleteLicense, setAutocompleteLicense] = useState<[string, string][]>([]);
  const ossNameOptions = Array.from(new Set(autocompleteOss.map((oss) => oss[0]))).map((oss) => ({
    value: oss,
    label: oss
  }));
  const ossVersionOptions = autocompleteOss
    .filter((oss) => oss[0] === watch('ossName'))
    .map((oss) => ({ value: oss[1], label: oss[1] }));
  const licenseOptions = autocompleteLicense.map((license) => ({
    value: license[1],
    label: `${license[0]} (${license[1]})`
  }));

  // Mode (add or edit)
  const isAdd = !values;

  // APIs for loading data for autocomplete
  const loadAutocompleteOSSRequest = useAPI(
    'get',
    'http://localhost:8180/api/lite/oss/autocomplete',
    {
      onSuccess: (res) => setAutocompleteOss(res.data)
    }
  );
  const loadAutocompleteLicenseRequest = useAPI(
    'get',
    'http://localhost:8180/api/lite/licenses/autocomplete',
    {
      onSuccess: (res) => setAutocompleteLicense(res.data)
    }
  );

  useEffect(() => {
    if (show) {
      setLicenses(values?.licenses || []);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  useEffect(() => {
    loadAutocompleteOSSRequest.execute({});
    loadAutocompleteLicenseRequest.execute({});

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
            <Controller
              name="ossName"
              control={control}
              render={({ field: { onChange, value, name, ref } }) => (
                <Select
                  classNames={{ control: () => '!border-darkgray !rounded-none' }}
                  name={name}
                  value={options.find((option) => option.value === value)}
                  options={ossNameOptions}
                  onChange={(selectedOption) => {
                    onChange(selectedOption?.value || '');
                  }}
                  placeholder="EX) fosslight"
                  ref={ref}
                />
              )}
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Version</label>
            <Controller
              name="ossVersion"
              control={control}
              render={({ field: { onChange, value, name, ref } }) => (
                <Select
                  classNames={{ control: () => '!border-darkgray !rounded-none' }}
                  name={name}
                  value={options.find((option) => option.value === value)}
                  options={ossVersionOptions}
                  onChange={(selectedOption) => {
                    onChange(selectedOption?.value || '');
                  }}
                  placeholder="EX) 1.0.0"
                  ref={ref}
                />
              )}
            />
          </div>
          <div className="col-span-2 flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Licenses</label>
            <Select
              classNames={{
                control: () => '!border-darkgray !rounded-none'
              }}
              options={licenseOptions}
              value={null}
              onChange={(selectedOption) => {
                if (!selectedOption) {
                  return;
                }

                const licenseName = selectedOption.value;
                if (!licenses.some((license) => license.licenseName === licenseName)) {
                  setLicenses([...licenses, { licenseId: null, licenseName }]);
                }
              }}
              placeholder="EX) Apache-2.0"
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
