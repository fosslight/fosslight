import clsx from 'clsx';
import Link from 'next/link';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { useState } from 'react';

function highlight(text: string, keyword?: string) {
  if (!keyword) {
    return text;
  }

  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const highlightedText = text.replace(
    new RegExp(escapedKeyword, 'gi'),
    (match) => `<span class="bg-yellow-200 text-semiblack">${match}</span>`
  );

  return <span dangerouslySetInnerHTML={{ __html: highlightedText }} />;
}

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
  const router = useRouter();
  const pathname = usePathname();
  const queryParams = useSearchParams();

  return (
    <div className="grid grid-cols-2 gap-4 w-[calc(100%-4px)]">
      <div className="col-span-2 shadow-box">
        <div className="flex items-center gap-x-3 text-sm">
          <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">Vulnerability</div>
          {searchKeyword === undefined && (
            <Link className="text-charcoal" href="/database/vulnerability?s=modify-dsc">
              show more here
            </Link>
          )}
          <div className="flex-1 text-right">
            <button
              className="no-tap-highlight"
              onClick={() => setIsVulnSectionShown(!isVulnSectionShown)}
            >
              {isVulnSectionShown ? (
                <i className="fa-solid fa-chevron-down" />
              ) : (
                <i className="fa-solid fa-chevron-up" />
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
                  <div
                    className="flex justify-center items-center flex-shrink-0 w-8 h-8 border border-crimson rounded-full text-crimson cursor-pointer"
                    onClick={() => {
                      const urlQueryParams = new URLSearchParams(queryParams);
                      urlQueryParams.set('modal-type', 'vuln');
                      urlQueryParams.set('modal-id', vulnerability.cveId);
                      router.push(`${pathname}?${urlQueryParams.toString()}`, {
                        scroll: false
                      });
                    }}
                  >
                    {vulnerability.cvssScore}
                  </div>
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div className="flex gap-x-1 font-semibold">
                        <div className="line-clamp-1 break-all">
                          {highlight(vulnerability.ossName, searchKeyword)}
                        </div>
                        <div className="flex-shrink-0">({vulnerability.ossVersion})</div>
                      </div>
                      <div className="flex-shrink-0 px-1 py-0.5 bg-darkgray rounded text-xs text-semiwhite">
                        {highlight(vulnerability.cveId, searchKeyword)}
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">
                      {vulnerability.summary}
                    </div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check" />
                      &ensp;
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
      <div className="col-span-2 self-start shadow-box lg:col-span-1">
        <div className="flex items-center gap-x-3 text-sm">
          <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">OSS</div>
          {searchKeyword === undefined && (
            <Link className="text-charcoal" href="/database/oss?s=modify-dsc">
              show more here
            </Link>
          )}
          <div className="flex-1 text-right">
            <button
              className="no-tap-highlight"
              onClick={() => setIsOssSectionShown(!isOssSectionShown)}
            >
              {isOssSectionShown ? (
                <i className="fa-solid fa-chevron-down" />
              ) : (
                <i className="fa-solid fa-chevron-up" />
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
                  className="flex pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div
                        className="flex gap-x-1 font-semibold cursor-pointer"
                        onClick={() => {
                          const urlQueryParams = new URLSearchParams(queryParams);
                          urlQueryParams.set('modal-type', 'oss');
                          urlQueryParams.set('modal-id', oss.ossId);
                          router.push(`${pathname}?${urlQueryParams.toString()}`, {
                            scroll: false
                          });
                        }}
                      >
                        <div className="line-clamp-1 break-all">
                          {highlight(oss.ossName, searchKeyword)}
                        </div>
                        <div className="flex-shrink-0">({oss.ossVersion})</div>
                      </div>
                      <div className="flex items-center gap-x-1 flex-shrink-0 p-1 border border-darkgray rounded text-xs">
                        {oss.obligations[0] === 'Y' && (
                          <i className="fa-solid fa-file-lines" title="Notice" />
                        )}
                        {oss.obligations[1] === 'Y' && (
                          <i className="fa-solid fa-code" title="Source" />
                        )}
                      </div>
                      <div
                        className="flex-shrink-0 px-1 py-0.5 border border-orange-500 rounded text-xs text-orange-500 cursor-pointer"
                        onClick={() => {
                          const urlQueryParams = new URLSearchParams(queryParams);
                          urlQueryParams.set('modal-type', 'vuln');
                          urlQueryParams.set('modal-id', oss.cveId);
                          router.push(`${pathname}?${urlQueryParams.toString()}`, {
                            scroll: false
                          });
                        }}
                      >
                        {oss.cvssScore}
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">
                      {highlight(oss.licenseName, searchKeyword)}
                    </div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check" />
                      &ensp;
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
      <div className="col-span-2 self-start shadow-box lg:col-span-1">
        <div className="flex items-center gap-x-3 text-sm">
          <div className="px-2 py-0.5 bg-charcoal rounded text-semiwhite">License</div>
          {searchKeyword === undefined && (
            <Link className="text-charcoal" href="/database/license?s=modify-dsc">
              show more here
            </Link>
          )}
          <div className="flex-1 text-right">
            <button
              className="no-tap-highlight"
              onClick={() => setIsLicenseSectionShown(!isLicenseSectionShown)}
            >
              {isLicenseSectionShown ? (
                <i className="fa-solid fa-chevron-down" />
              ) : (
                <i className="fa-solid fa-chevron-up" />
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
                  className="flex pb-3 border-b border-b-semigray last:pb-0 last:border-none"
                >
                  <div className="flex flex-col gap-y-1">
                    <div className="flex gap-x-2 items-center">
                      <div
                        className="flex gap-x-1 font-semibold cursor-pointer"
                        onClick={() => {
                          const urlQueryParams = new URLSearchParams(queryParams);
                          urlQueryParams.set('modal-type', 'license');
                          urlQueryParams.set('modal-id', license.licenseId);
                          router.push(`${pathname}?${urlQueryParams.toString()}`, {
                            scroll: false
                          });
                        }}
                      >
                        <div className="line-clamp-1 break-all">
                          {highlight(license.licenseName, searchKeyword)}
                        </div>
                        <div className="flex-shrink-0">
                          ({highlight(license.licenseIdentifier, searchKeyword)})
                        </div>
                      </div>
                      <div className="flex items-center gap-x-1 flex-shrink-0 p-1 border border-darkgray rounded text-xs">
                        {license.obligations[0] === 'Y' && (
                          <i className="fa-solid fa-file-lines" title="Notice" />
                        )}
                        {license.obligations[1] === 'Y' && (
                          <i className="fa-solid fa-code" title="Source" />
                        )}
                      </div>
                    </div>
                    <div className="line-clamp-3 text-sm text-semiblack/80">
                      {license.restrictions.join(', ')}
                    </div>
                    <div className="text-sm text-darkgray">
                      <i className="fa-solid fa-check" />
                      &ensp;
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
