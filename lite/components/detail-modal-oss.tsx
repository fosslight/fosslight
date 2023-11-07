export default function DetailModalOSS({ data }: { data: any }) {
  return (
    <>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        <i className="text-sm fa-solid fa-cube" />
        &ensp;OSS Detail
      </div>
      {JSON.stringify(data)}
    </>
  );
}

/*
OSS ID
OSS Name
OSS Nickname
OSS Version
OSS Type
License Name(s)
License Type
Declared License(s)
Detected License(s)
Obligation(s)
Download URL
Homepage URL
Description
Copyright
Attribution
Vulnerability(s)
Deactivate
Creator
Created
Modifier
Modified
*/
