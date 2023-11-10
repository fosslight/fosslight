import DetailModalRow from './detail-modal-row';

export default function DetailModalLicense({ data }: { data: any }) {
  return (
    <>
      <div className="pb-6 text-lg font-black">
        <i className="text-base fa-solid fa-cube" />
        &ensp;License Detail
      </div>
      <div className="flex flex-col gap-y-5 lg:gap-y-3">
        <DetailModalRow
          label="Name"
          value={
            <div className="flex flex-col gap-y-3">
              {data.licenseName}
              <div className="flex gap-x-1.5 items-center text-sm">
                <span className="px-1 bg-semiblack rounded text-xs text-semiwhite">
                  SPDX Identifier
                </span>
                {data.licenseIdentifier}
              </div>
              <details className="px-2.5 py-1.5 border border-semigray rounded text-sm">
                <summary className="outline-none cursor-pointer no-tap-highlight">
                  Nicknames
                </summary>
                <div className="mt-1">
                  {(data.licenseNicknames as string[]).map((licenseNickname, idx) => (
                    <div key={idx} className="flex gap-x-1.5 items-center">
                      <span className="px-1 bg-semiblack rounded text-xs text-semiwhite">N</span>
                      {licenseNickname}
                    </div>
                  ))}
                </div>
              </details>
            </div>
          }
          bottomBorder
        />
        <DetailModalRow label="Type" value={data.licenseType} bottomBorder />
        <DetailModalRow
          label="Obligations"
          value={(() => {
            const notice = data.obligations[0] === 'Y';
            const source = data.obligations[1] === 'Y';

            if (!notice && !source) {
              return null;
            }

            return (
              <div className="flex flex-col gap-y-3">
                {notice && (
                  <div className="flex gap-x-2 items-center">
                    <i className="text-sm fa-solid fa-file-lines" title="Notice" />
                    <span className="font-semibold text-crimson">
                      You must notify(generate notice).
                    </span>
                  </div>
                )}
                {source && (
                  <div className="flex gap-x-2 items-center">
                    <i className="text-sm fa-solid fa-code" title="Source" />
                    <span className="font-semibold text-crimson">
                      You must disclose the source code.
                    </span>
                  </div>
                )}
              </div>
            );
          })()}
        />
        <DetailModalRow
          label="Restrictions"
          value={<div className="whitespace-pre-line">{data.restrictions.join('\n')}</div>}
          bottomBorder
        />
        <DetailModalRow
          label="Homepage URL"
          value={
            <a className="text-blue-500 hover:underline" href={data.homepageUrl} target="_blank">
              {data.homepageUrl}
            </a>
          }
          bottomBorder
        />
        <DetailModalRow
          label="Description"
          value={<div className="whitespace-pre-line">{data.description}</div>}
        />
        <DetailModalRow
          label="License Text"
          value={<div className="whitespace-pre-line">{data.licenseText}</div>}
        />
        <DetailModalRow
          label="Attribution"
          value={<div className="whitespace-pre-line">{data.attribution}</div>}
          bottomBorder
        />
        <DetailModalRow
          label="Create"
          value={`${data.created.substring(0, 16)} (${data.creator})`}
        />
        <DetailModalRow
          label="Modify"
          value={`${data.modified.substring(0, 16)} (${data.modifier})`}
        />
      </div>
    </>
  );
}
