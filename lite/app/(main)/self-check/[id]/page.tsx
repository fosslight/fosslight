export default function SelfCheckDetail({ params }: { params: { id: string } }) {
  return (
    <>
      {/* Breadcrumb */}
      <h2 className="breadcrumb">
        Self-Check List
        <i className="mx-2 text-sm fa-solid fa-angle-right"></i>
        Project {params.id}
      </h2>

      {/* Description */}
      <h3 className="pb-8">Detailed information of a project with ID {params.id}.</h3>

      {/* Basic information */}
      <div className="w-[calc(100%-4px)] shadow-box">
        <div className="flex flex-col gap-x-8 gap-y-2 text-sm lg:flex-row">
          <div className="flex flex-col gap-y-2 flex-1">
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Project Name</div>
              <div className="flex-1 text-semiblack/80">FOSSLight Hub Lite</div>
            </div>
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Project Version</div>
              <div className="flex-1 text-semiblack/80">1.0.0</div>
            </div>
          </div>
          <div className="flex flex-col gap-y-2 flex-1">
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Create</div>
              <div className="flex-1 text-semiblack/80">2023-10-05 23:54</div>
            </div>
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Comment</div>
              <div className="flex-1 text-semiblack/80">There are some comments here.</div>
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-x-1 mt-2">
          <button className="px-2 py-0.5 crimson-btn">Delete</button>
          <button className="px-2 py-0.5 default-btn">Edit</button>
        </div>
      </div>
    </>
  );
}
