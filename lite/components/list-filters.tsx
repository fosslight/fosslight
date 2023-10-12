export default function ListFilters() {
  return (
    <form className="w-[calc(100%-4px)] p-4 border border-darkgray rounded-lg shadow-[2px_2px_4px_0_rgba(0,0,0,0.2)]">
      <div className="grid grid-cols-1 gap-x-8 gap-y-2 text-sm lg:grid-cols-2">
        <div className="flex items-start gap-x-4">
          <div className="flex-shrink-0 w-20">Text</div>
          <div className="flex-1">
            <div className="flex gap-x-2">
              <input className="flex-1 w-0 px-2 py-1 border border-darkgray outline-none" />
            </div>
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className="flex-shrink-0 w-20">Text + Check</div>
          <div className="flex-1">
            <div className="flex gap-x-2">
              <input className="flex-1 w-0 px-2 py-1 border border-darkgray outline-none" />
              <label className="flex items-center gap-x-1.5">
                <input className="w-4 h-4" type="checkbox" />
                Exact
              </label>
            </div>
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className="flex-shrink-0 w-20">Select</div>
          <div className="flex-1">
            <select className="w-full px-2 py-1 border border-darkgray outline-none">
              <option>Option 1</option>
              <option>Option 2</option>
              <option>Option 3</option>
            </select>
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className="flex-shrink-0 w-20">Check</div>
          <div className="flex-1">
            <div className="flex flex-wrap gap-x-4 gap-y-1">
              <label className="flex items-center gap-x-1.5">
                <input className="w-4 h-4" type="checkbox" />
                Label 1
              </label>
              <label className="flex items-center gap-x-1.5">
                <input className="w-4 h-4" type="checkbox" />
                Label 2
              </label>
              <label className="flex items-center gap-x-1.5">
                <input className="w-4 h-4" type="checkbox" />
                Label 3
              </label>
              <label className="flex items-center gap-x-1.5">
                <input className="w-4 h-4" type="checkbox" />
                Label 4
              </label>
            </div>
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className="flex-shrink-0 w-20">Date</div>
          <div className="flex-1">
            <div className="flex gap-x-2">
              <input className="flex-1 w-0 px-2 py-1 border border-darkgray outline-none" />
              ~
              <input className="flex-1 w-0 px-2 py-1 border border-darkgray outline-none" />
            </div>
          </div>
        </div>
        <div className="flex items-start gap-x-4">
          <div className="flex-shrink-0 w-20">TextArea</div>
          <div className="flex-1">
            <textarea
              className="block w-full px-2 py-1 border border-darkgray outline-none resize-none"
              rows={3}
            ></textarea>
          </div>
        </div>
      </div>
      <div className="mt-2 text-right">
        <button className="px-2 py-1 bg-crimson rounded text-semiwhite">Search</button>
      </div>
    </form>
  );
}
