import Modal from './modal';

export default function SelfCheckNoticeWarningModal({
  show,
  onHide,
  sendEmail
}: {
  show: boolean;
  onHide: () => void;
  sendEmail: () => void;
}) {
  return (
    <Modal show={show} onHide={onHide} size="sm">
      <div className="mb-4">
        There are some <span className="font-semibold text-crimson">incorrect license</span>{' '}
        information. If you want to generate the appropriate notice,{' '}
        <span className="font-semibold text-crimson">please fix it in the OSS tab first</span>.
      </div>
      <div className="mb-4 text-darkgray">
        If you want to inform your administrator, click the button below to send an email.
      </div>
      <button className="px-2 py-0.5 charcoal-btn" onClick={() => sendEmail()}>
        Send an email to admin
      </button>
    </Modal>
  );
}
