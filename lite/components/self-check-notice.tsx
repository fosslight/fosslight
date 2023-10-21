import clsx from 'clsx';
import { useState } from 'react';

export default function SelfCheckNotice() {
  const [method, setMethod] = useState<'default' | 'custom'>('default');
  const [companyName, setCompanyName] = useState<string | null>('');
  const [ossSite, setOssSite] = useState<string | null>('');
  const [email, setEmail] = useState<string | null>('');
  const [isVerShown, setIsVerShown] = useState(true);
  const [append, setAppend] = useState<string | null>(null);

  const rowClass = 'flex flex-col gap-2 lg:flex-row';
  const labelWrapperClass = 'flex-shrink-0 w-44';
  const labelClass = 'inline-flex items-center gap-x-2';
  const inputWrapperClass = 'flex-1';
  const inputClass = 'w-60 max-w-full px-2 py-0.5 mb-1.5 border border-darkgray outline-none';
  const commentClass = 'text-xs text-darkgray';

  return (
    <div className="w-[calc(100%-4px)] shadow-box">
      {/* Default vs Custom */}
      <div className="flex flex-col items-start gap-y-2">
        <label className="flex items-center gap-x-2">
          <input
            type="radio"
            value="file"
            checked={method === 'default'}
            onChange={() => setMethod('default')}
          />
          Generate Default Notice
        </label>
        <label className="flex items-center gap-x-2">
          <input
            type="radio"
            value="url"
            checked={method === 'custom'}
            onChange={() => setMethod('custom')}
          />
          Generate Custom Notice
        </label>
      </div>
      <div className="flex justify-end gap-x-1 mt-4 mb-2">
        <button className="px-2 py-0.5 crimson-btn">Generate</button>
        <button className="px-2 py-0.5 default-btn">Preview</button>
      </div>

      {/* Customize */}
      <div
        className={clsx(
          'flex flex-col gap-y-3 p-4 border border-darkgray rounded-lg text-sm',
          method === 'default' && 'bg-semiwhite'
        )}
      >
        <div className={rowClass}>
          <div className={labelWrapperClass}>
            <label className={labelClass}>
              <input
                type="checkbox"
                checked={companyName !== null}
                disabled={method === 'default'}
                onChange={(e) => setCompanyName(e.target.checked ? '' : null)}
              />
              Company Name
            </label>
          </div>
          <div className={inputWrapperClass}>
            <input
              className={inputClass}
              value={companyName || ''}
              disabled={method === 'default' || companyName === null}
              onChange={(e) => setCompanyName(e.target.value)}
            />
            <div className={commentClass}>* Deselect if the company name must be removed.</div>
          </div>
        </div>
        <div className={rowClass}>
          <div className={labelWrapperClass}>
            <label className={labelClass}>
              <input
                type="checkbox"
                checked={ossSite !== null}
                disabled={method === 'default'}
                onChange={(e) => setOssSite(e.target.checked ? '' : null)}
              />
              OSS Distribution Site
            </label>
          </div>
          <div className={inputWrapperClass}>
            <input
              className={inputClass}
              value={ossSite || ''}
              disabled={method === 'default' || ossSite === null}
              onChange={(e) => setOssSite(e.target.value)}
            />
            <div className={commentClass}>
              * Deselect if both OSS Package and OSS Notice are not registered on the OSS
              Distribution site.
            </div>
          </div>
        </div>
        <div className={rowClass}>
          <div className={labelWrapperClass}>
            <label className={labelClass}>
              <input
                type="checkbox"
                checked={email !== null}
                disabled={method === 'default'}
                onChange={(e) => setEmail(e.target.checked ? '' : null)}
              />
              Email (Written Offer)
            </label>
          </div>
          <div className={inputWrapperClass}>
            <input
              className={inputClass}
              value={email || ''}
              disabled={method === 'default' || email === null}
              onChange={(e) => setEmail(e.target.value)}
            />
            <div className={commentClass}>
              * Deselect if the written offer is not required(OSS Package is delivered directly to
              the recipient).
            </div>
          </div>
        </div>
        <div className={rowClass}>
          <div className={labelWrapperClass}>
            <label className={labelClass}>
              <input
                type="checkbox"
                checked={!isVerShown}
                disabled={method === 'default'}
                onChange={(e) => setIsVerShown(!e.target.checked)}
              />
              Hide OSS Version
            </label>
          </div>
          <div className={inputWrapperClass}>
            <div className={commentClass}>
              * Select if all OSS version information must be removed.
            </div>
          </div>
        </div>
        <div className={rowClass}>
          <div className={labelWrapperClass}>
            <label className={labelClass}>
              <input
                type="checkbox"
                checked={append !== null}
                disabled={method === 'default'}
                onChange={(e) => setAppend(e.target.checked ? '' : null)}
              />
              Append
            </label>
          </div>
          <div className={inputWrapperClass}>
            <div className={commentClass}>
              * Select if there is something to add to the OSS Notice
            </div>
            {append !== null && (
              <textarea
                className="w-full p-2 mt-2 border border-darkgray outline-none resize-none"
                rows={6}
                value={append}
                disabled={method === 'default' || append === null}
                onChange={(e) => setAppend(e.target.value)}
              />
            )}
          </div>
        </div>
      </div>

      {/* Warning */}
      <div className="mt-4 text-sm text-semiblack/50 leading-relaxed">
        * This notice is for reference only, and FOSSLight Hub makes no warranty on the content,
        reliability, accuracy, etc. of this notice. All responsibilities arising from using this
        notice is on you, and FOSSLight Hub has no responsibility to you and any third party.
      </div>
    </div>
  );
}
