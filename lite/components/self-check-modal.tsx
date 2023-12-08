import Modal from './modal';

export default function SelfCheckModal({
  show,
  onHide,
  values
}: {
  show: boolean;
  onHide: () => void;
  values?: SelfCheck.Edit;
}) {
  return (
    <Modal show={show} onHide={onHide} size="sm" hideByBackdrop={false}>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        {!values ? (
          <i className="text-sm fa-solid fa-plus" />
        ) : (
          <i className="text-sm fa-solid fa-pen" />
        )}
        &ensp;
        {!values ? 'Create a project to check' : 'Edit the project'}
      </div>
      <div className="flex flex-col gap-y-4">
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">
            Project Name <span className="font-bold text-crimson">*</span>
          </label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) FOSSLight Hub Lite"
            defaultValue={values?.projectName || ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Project Version</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) 1.0.0"
            defaultValue={values?.projectVersion || ''}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Comment</label>
          <textarea
            className="w-full px-2 py-1 border border-darkgray outline-none resize-none"
            rows={3}
            defaultValue={values?.comment || ''}
          />
        </div>
      </div>
      <div className="flex justify-end gap-x-1 mt-4">
        <button className="px-2 py-0.5 crimson-btn">{!values ? 'Create' : 'Edit'}</button>
        <button className="px-2 py-0.5 default-btn" onClick={onHide}>
          Cancel
        </button>
      </div>
    </Modal>
  );
}
