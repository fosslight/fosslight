import { useAPI } from '@/lib/hooks';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import Editor from './editor';
import Modal from './modal';

export default function SelfCheckModal({
  show,
  onHide,
  values,
  refetch
}: {
  show: boolean;
  onHide: () => void;
  values?: SelfCheck.Edit;
  refetch?: () => void;
}) {
  const [wait, setWait] = useState(false);
  const router = useRouter();
  const { register, handleSubmit, watch, setValue } = useForm({
    defaultValues: {
      projectName: values?.projectName || '',
      projectVersion: values?.projectVersion || '',
      comment: values?.comment || ''
    }
  });

  // Mode (create or edit)
  const isCreate = !values;

  // API for creating/editing project
  const createEditProjectRequest = useAPI('post', '/selfCheck/saveAjax', {
    onStart: () => setWait(true),
    onSuccess: (res) => {
      if (res.data.isValid) {
        alert(`Successfully ${isCreate ? 'created' : 'edited'} project`);

        onHide();
        if (isCreate) {
          router.push(`/self-check/${res.data.resultData.prjId}`);
        } else if (refetch) {
          refetch();
        }
      } else {
        alert(`Failed in ${isCreate ? 'creating' : 'editing'} project`);
      }
    },
    onFinish: () => setWait(false)
  });

  return (
    <Modal show={show} onHide={onHide} size="sm" hideByBackdrop={false}>
      <form
        onSubmit={handleSubmit((data) => {
          if (!window.confirm('Are you sure to continue?')) {
            return;
          }

          createEditProjectRequest.execute({
            body: {
              prjId: values?.projectId,
              prjName: data.projectName,
              prjVersion: data.projectVersion,
              comment: data.comment
            }
          });
        })}
      >
        <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
          {isCreate ? (
            <i className="text-sm fa-solid fa-plus" />
          ) : (
            <i className="text-sm fa-solid fa-pen" />
          )}
          &ensp;
          {isCreate ? 'Create a project to check' : 'Edit the project'}
        </div>
        <div className="flex flex-col gap-y-4">
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">
              Project Name <span className="font-bold text-crimson">*</span>
            </label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              placeholder="EX) FOSSLight Hub Lite"
              {...register('projectName', { required: true })}
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Project Version</label>
            <input
              className="w-full px-2 py-1 border border-darkgray outline-none"
              placeholder="EX) 1.0.0"
              {...register('projectVersion')}
            />
          </div>
          <div className="flex flex-col gap-y-2">
            <label className="text-sm font-semibold">Comment</label>
            <Editor value={watch('comment')} setValue={(value) => setValue('comment', value)} />
          </div>
        </div>
        <div className="flex justify-end gap-x-1 mt-4">
          <button className="px-2 py-0.5 crimson-btn" disabled={wait}>
            {isCreate ? 'Create' : 'Edit'}
          </button>
          <button className="px-2 py-0.5 default-btn" type="button" onClick={onHide}>
            Cancel
          </button>
        </div>
      </form>
    </Modal>
  );
}
