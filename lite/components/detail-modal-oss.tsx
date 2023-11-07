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
OSS Type
OSS Name
OSS Nickname
OSS Version
Download URL
Homepage URL
Description
Copyright
Attribution
Creator
Created
Modifier
Modified
Deactivate

License Name(s)
License Type
Declared License(s)
Detected License(s)
Obligation(s)

Vulnerability(s)
*/
