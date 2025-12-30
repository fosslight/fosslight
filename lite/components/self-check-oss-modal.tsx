import { useAPI } from '@/lib/hooks';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import InputWithAutocomplete from './input-with-autocomplete';
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
  const { register, handleSubmit, reset, watch, setValue } = useForm({
    defaultValues: {
      ossName: values?.ossName || '',
      ossVersion: values?.ossVersion || '',
      licenseName: '',
      path: values?.path || '',
      copyright: values?.copyright || '',
      downloadUrl: values?.downloadUrl || '',
      homepageUrl: values?.homepageUrl || ''
    }
  });

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
  const loadAutocompleteOSSRequest = useAPI('get', '/api/lite/oss/candidates/all', {
    onSuccess: (res) => setAutocompleteOss(res.data)
  });
  const loadAutocompleteLicenseRequest = useAPI('get', '/api/lite/licenses/candidates/all', {
    onSuccess: (res) => setAutocompleteLicense(res.data)
  });
  const loadOSSLicenseRequest = useAPI('get', '/project/getOssIdLicenses', {
    onSuccess: (res) => {
      const { prjOssMaster: oss, prjLicense: licenseList } = res.data;

      if (oss && licenseList && licenseList.length > 0) {
        const {
          copyrightText: copyright,
          downloadLocation: downloadUrl,
          homepage: homepageUrl
        } = oss;
        const license = licenseList[0];

        const confirmMsg = [
          'Do you want to fill the following fields automatically?\n',
          `- License: ${license.licenseName}`,
          '- Copyright',
          '- Download URL',
          '- Homepage URL'
        ].join('\n');

        if (window.confirm(confirmMsg)) {
          setLicenses([{ licenseId: license.licenseId, licenseName: license.licenseName }]);
          setValue('copyright', copyright);
          setValue('downloadUrl', downloadUrl);
          setValue('homepageUrl', homepageUrl);
        }
      }
    }
  });

  useEffect(() => {
    if (show) {
      loadAutocompleteOSSRequest.execute({});
      loadAutocompleteLicenseRequest.execute({});

      reset({
        ossName: values?.ossName || '',
        ossVersion: values?.ossVersion || '',
        licenseName: '',
        path: values?.path || '',
        copyright: values?.copyright || '',
        downloadUrl: values?.downloadUrl || '',
        homepageUrl: values?.homepageUrl || ''
      });
      setLicenses(values?.licenses || []);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  return (
    <Modal show={show} onHide={onHide} size="lg" hideByBackdrop={false}>
      <form
        onSubmit={handleSubmit((data: any) => {
          const formData = { ...data, licenses };

          // Add
          if (isAdd) {
            setOssList([
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
              },
              ...ossList
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
            <InputWithAutocomplete
              value={watch('ossName')}
              setValue={(value) => setValue('ossName', value)}
              options={ossNameOptions}
              placeholder="EX) fosslight"
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Version</label>
            <InputWithAutocomplete
              value={watch('ossVersion')}
              setValue={(value) => setValue('ossVersion', value)}
              onBlur={() => {
                const { ossName, ossVersion } = watch();
                loadOSSLicenseRequest.execute({ params: { ossName, ossVersion } });
              }}
              options={ossVersionOptions}
              placeholder="EX) 1.0.0"
            />
          </div>
          <div className="col-span-2 flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Licenses</label>
            <InputWithAutocomplete
              value={watch('licenseName')}
              setValue={(value) => setValue('licenseName', value)}
              pickValue={(value: string) => {
                if (!value) {
                  return;
                }

                if (!licenses.some((license) => license.licenseName === value)) {
                  setLicenses([...licenses, { licenseId: null, licenseName: value }]);
                }
                setValue('licenseName', '');
              }}
              options={licenseOptions}
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
