export default function DetailModalLicense({ data }: { data: any }) {
  return (
    <>
      <div className="pb-4 mb-4 border-b border-b-semigray font-bold">
        <i className="text-sm fa-regular fa-id-card" />
        &ensp;License Detail
      </div>
      {JSON.stringify(data)}
    </>
  );
}

/*
License ID
License Name
License Nickname
License Identifier
License Type
Obligation(s)
Restriction(s)
Homepage URL
Description
License Text
Attribution
Creator
Created
Modifier
Modified
*/
