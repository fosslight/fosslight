'use client';

import Modal from './modal';

export default function SelfCheckModal({
  mode,
  data,
  show,
  onHide
}: {
  mode: 'create' | 'edit';
  data: { name: string; version: string; comment: string };
  show: boolean;
  onHide: () => void;
}) {
  return (
    <Modal show={show} onHide={onHide} hideByBackdrop={false}>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        {mode === 'create' ? (
          <i className="text-sm fa-solid fa-plus" />
        ) : (
          <i className="text-sm fa-solid fa-pen" />
        )}
        &ensp;
        {mode === 'create' ? 'Create a project to check' : 'Edit the project'}
      </div>
      <div className="flex flex-col gap-y-4 w-72">
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">
            Project Name <span className="font-bold text-crimson">*</span>
          </label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) FOSSLight Hub Lite"
            value={data.name}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Project Version</label>
          <input
            className="w-full px-2 py-1 border border-darkgray outline-none"
            placeholder="EX) 1.0.0"
            value={data.version}
          />
        </div>
        <div className="flex flex-col gap-y-2">
          <label className="text-sm font-semibold">Comment</label>
          <textarea
            className="w-full px-2 py-1 border border-darkgray outline-none resize-none"
            rows={3}
          >
            {data.comment}
          </textarea>
        </div>
      </div>
      <div className="flex justify-end gap-x-1 mt-4">
        <button className="px-2 py-0.5 crimson-btn">{mode === 'create' ? 'Create' : 'Edit'}</button>
        <button className="px-2 py-0.5 default-btn" onClick={onHide}>
          Cancel
        </button>
      </div>
    </Modal>
  );
}
