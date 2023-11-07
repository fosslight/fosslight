export default function DetailModalVuln({ data }: { data: any }) {
  return (
    <>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        <i className="text-sm fa-solid fa-triangle-exclamation" />
        &ensp;Vulnerability Detail
      </div>
      {JSON.stringify(data)}
    </>
  );
}

/*
CVE ID
CVSS Score
Summary
Published
Modified

OSS Name
OSS Version
Vendor
*/
