import clsx from 'clsx';
import Link from 'next/link';
import { useState } from 'react';

export default function ListSections({
  vulnerabilityList,
  ossList,
  licenseList,
  searchKeyword
}: {
  vulnerabilityList: any[];
  ossList: any[];
  licenseList: any[];
  searchKeyword?: string;
}) {
  const [isVulnSectionShown, setIsVulnSectionShown] = useState(true);
  const [isOssSectionShown, setIsOssSectionShown] = useState(true);
  const [isLicenseSectionShown, setIsLicenseSectionShown] = useState(true);
  const sectionClass =
    'p-4 border border-darkgray rounded-lg shadow-[2px_2px_4px_0_rgba(0,0,0,0.2)]';

  return (
    <div className="grid grid-cols-2 gap-4 w-[calc(100%-4px)]">
      <div className={clsx('col-span-2', sectionClass)}>
        <div className="flex items-center gap-x-3 text-sm">
          <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">Vulnerability</div>
          {searchKeyword === undefined && (
            <Link className="text-charcoal" href="/database/vulnerability?s=mod-dsc">
              show more here
            </Link>
          )}
          <div className="flex-1 text-right">
            <button
              className="no-tap-highlight"
              onClick={() => setIsVulnSectionShown(!isVulnSectionShown)}
            >
              {isVulnSectionShown ? (
                <i className="fa-solid fa-chevron-down"></i>
              ) : (
                <i className="fa-solid fa-chevron-up"></i>
              )}
            </button>
          </div>
        </div>
        <div
          className={clsx(
            'overflow-y-hidden transition-[max-height] duration-500',
            isVulnSectionShown ? 'max-h-[1000px]' : 'max-h-0'
          )}
        >
          <div className="flex flex-col gap-y-3 mt-4">
            {vulnerabilityList.length > 0 ? (
              vulnerabilityList.map((vulnerability, idx) => (
                <div
                  key={idx}
                  className="flex gap-x-3 pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex justify-center items-center flex-shrink-0 w-8 h-8 border border-crimson rounded-full text-crimson">
                    {vulnerability.cvssScore}
                  </div>
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div className="line-clamp-1 font-semibold break-all">
                        {vulnerability.ossName}
                      </div>
                      <div className="flex-shrink-0 font-semibold">
                        ({vulnerability.ossVersion})
                      </div>
                      <div className="flex-shrink-0 px-1 py-0.5 bg-darkgray rounded text-xs text-semiwhite">
                        {vulnerability.cveId}
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">
                      {vulnerability.summary}
                    </div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check"></i>&ensp;
                      {vulnerability.published < vulnerability.modified
                        ? `${vulnerability.modified.substring(0, 10)} modified`
                        : `${vulnerability.published.substring(0, 10)} published`}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-darkgray">
                {searchKeyword === undefined ? 'No entries' : 'No results'}
              </div>
            )}
          </div>
        </div>
      </div>
      <div className={clsx('col-span-2 lg:col-span-1', sectionClass)}>
        <div className="flex items-center gap-x-3 text-sm">
          <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">OSS</div>
          {searchKeyword === undefined && (
            <Link className="text-charcoal" href="/database/oss?s=mod-dsc">
              show more here
            </Link>
          )}
          <div className="flex-1 text-right">
            <button
              className="no-tap-highlight"
              onClick={() => setIsOssSectionShown(!isOssSectionShown)}
            >
              {isOssSectionShown ? (
                <i className="fa-solid fa-chevron-down"></i>
              ) : (
                <i className="fa-solid fa-chevron-up"></i>
              )}
            </button>
          </div>
        </div>
        <div
          className={clsx(
            'overflow-y-hidden transition-[max-height] duration-500',
            isOssSectionShown ? 'max-h-[1000px]' : 'max-h-0'
          )}
        >
          <div className="flex flex-col gap-y-3 mt-4">
            {ossList.length > 0 ? (
              ossList.map((oss, idx) => (
                <div
                  key={idx}
                  className="flex gap-x-3 pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div className="line-clamp-1 font-semibold break-all">{oss.ossName}</div>
                      <div className="flex-shrink-0 font-semibold">({oss.ossVersion})</div>
                      <div className="flex items-center gap-x-1 flex-shrink-0 px-1 py-1 border border-darkgray rounded text-xs">
                        {oss.obligations[0] === 'Y' && (
                          <i className="fa-solid fa-file-lines" title="Notice"></i>
                        )}
                        {oss.obligations[1] === 'Y' && (
                          <i className="fa-solid fa-code" title="Source"></i>
                        )}
                      </div>
                      <div className="flex-shrink-0 px-1 py-0.5 border border-orange-500 rounded text-xs">
                        <span className="text-orange-500">{oss.cvssScore}</span>
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">{oss.licenseName}</div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check"></i>&ensp;
                      {oss.created < oss.modified
                        ? `${oss.modified.substring(0, 10)} modified`
                        : `${oss.created.substring(0, 10)} created`}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-darkgray">
                {searchKeyword === undefined ? 'No entries' : 'No results'}
              </div>
            )}
          </div>
        </div>
      </div>
      <div className={clsx('col-span-2 lg:col-span-1', sectionClass)}>
        <div className="flex items-center gap-x-3 text-sm">
          <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">License</div>
          {searchKeyword === undefined && (
            <Link className="text-charcoal" href="/database/license?s=mod-dsc">
              show more here
            </Link>
          )}
          <div className="flex-1 text-right">
            <button
              className="no-tap-highlight"
              onClick={() => setIsLicenseSectionShown(!isLicenseSectionShown)}
            >
              {isLicenseSectionShown ? (
                <i className="fa-solid fa-chevron-down"></i>
              ) : (
                <i className="fa-solid fa-chevron-up"></i>
              )}
            </button>
          </div>
        </div>
        <div
          className={clsx(
            'overflow-y-hidden transition-[max-height] duration-500',
            isLicenseSectionShown ? 'max-h-[1000px]' : 'max-h-0'
          )}
        >
          <div className="flex flex-col gap-y-3 mt-4">
            {licenseList.length > 0 ? (
              licenseList.map((license, idx) => (
                <div
                  key={idx}
                  className="flex gap-x-3 pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div className="line-clamp-1 font-semibold break-all">
                        {license.licenseName}
                      </div>
                      <div className="flex-shrink-0 font-semibold">
                        ({license.licenseIdentifier})
                      </div>
                      <div className="flex items-center gap-x-1 flex-shrink-0 px-1 py-1 border border-darkgray rounded text-xs">
                        {license.obligations[0] === 'Y' && (
                          <i className="fa-solid fa-file-lines" title="Notice"></i>
                        )}
                        {license.obligations[1] === 'Y' && (
                          <i className="fa-solid fa-code" title="Source"></i>
                        )}
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">
                      {license.restrictions.join(', ')}
                    </div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check"></i>&ensp;
                      {license.created < license.modified
                        ? `${license.modified.substring(0, 10)} modified`
                        : `${license.created.substring(0, 10)} created`}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-darkgray">
                {searchKeyword === undefined ? 'No entries' : 'No results'}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
