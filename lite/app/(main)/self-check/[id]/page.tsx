'use client';

import SelfCheckModal from '@/components/self-check-modal';
import SelfCheckNotice from '@/components/self-check-notice';
import SelfCheckOSS from '@/components/self-check-oss';
import SelfCheckPackage from '@/components/self-check-package';
import { loadingState } from '@/lib/atoms';
import { useAPI } from '@/lib/hooks';
import { SELF_CHECK_TABS } from '@/lib/literals';
import clsx from 'clsx';
import { useRouter } from 'next/navigation';
import { Fragment, useEffect, useState } from 'react';
import { useSetRecoilState } from 'recoil';

export default function SelfCheckDetail({ params }: { params: { id: string } }) {
  const setLoading = useSetRecoilState(loadingState);
  const [data, setData] = useState<SelfCheck.Basics>();
  const [wait, setWait] = useState(false);
  const [isModalShown, setIsModalShown] = useState(false);
  const [changed, setChanged] = useState(false);
  const [tab, setTab] = useState<SelfCheck.Tab['name']>('OSS');
  const router = useRouter();

  // API for loading data
  const loadDataRequest = useAPI('get', `/api/lite/selfchecks/${params.id}`, {
    onStart: () => setLoading(true),
    onSuccess: (res) => {
      setData(res.data.selfCheck);
    },
    onFinish: () => setLoading(false)
  });

  // API for deleting project
  const deleteProjectRequest = useAPI('post', '/selfCheck/delAjax', {
    onStart: () => setWait(true),
    onSuccess: () => {
      alert('Successfully deleted project');
      router.push('/self-check');
    },
    onFinish: () => setWait(false)
  });

  useEffect(() => {
    loadDataRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* Breadcrumb */}
      <h2 className="breadcrumb">
        Self-Check List
        <i className="mx-2 text-sm fa-solid fa-angle-right" />
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
              <div className="flex-1 text-semiblack/80">{data?.projectName || ''}</div>
            </div>
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Project Version</div>
              <div className="flex-1 text-semiblack/80">{data?.projectVersion || ''}</div>
            </div>
          </div>
          <div className="flex flex-col gap-y-2 flex-1">
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Create</div>
              <div className="flex-1 text-semiblack/80">{data?.created.substring(0, 16) || ''}</div>
            </div>
            <div className="flex">
              <div className="flex-shrink-0 w-32 font-semibold">Comment</div>
              <div
                className="flex-1 text-semiblack/80"
                dangerouslySetInnerHTML={{ __html: data?.comment || '' }}
              />
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-x-1 mt-2">
          <button
            className="px-2 py-0.5 crimson-btn"
            onClick={() => {
              if (!window.confirm('Are you sure to continue?')) {
                return;
              }

              deleteProjectRequest.execute({ body: { prjId: params.id, deleteMemo: '' } });
            }}
            disabled={wait}
          >
            Delete
          </button>
          <button
            className="px-2 py-0.5 default-btn"
            onClick={() => setIsModalShown(true)}
            disabled={!data}
          >
            Edit
          </button>
        </div>
      </div>
      {data && (
        <SelfCheckModal
          show={isModalShown}
          onHide={() => setIsModalShown(false)}
          values={{
            projectId: params.id,
            projectName: data.projectName,
            projectVersion: data.projectVersion,
            comment: data.comment
          }}
          refetch={() => loadDataRequest.execute({})}
        />
      )}

      {/* Tab selector */}
      <div className="flex justify-center items-center gap-x-2 mt-12 text-sm font-semibold">
        {SELF_CHECK_TABS.map((selfCheckTab, idx) => {
          return (
            <Fragment key={selfCheckTab.name}>
              <button
                className={clsx(
                  'px-3 py-2 border rounded transition-colors duration-300 no-tap-highlight',
                  tab === selfCheckTab.name
                    ? 'bg-charcoal border-charcoal text-semiwhite'
                    : 'border-darkgray text-darkgray'
                )}
                disabled={tab === selfCheckTab.name}
                onClick={() => {
                  if (changed && idx > 0) {
                    alert('You should save first');
                    document
                      .getElementById('oss-scroll-pos')
                      ?.scrollIntoView({ behavior: 'smooth' });
                    return;
                  }

                  setTab(selfCheckTab.name);
                }}
              >
                {idx + 1}. {selfCheckTab.name}
              </button>
              {idx < 2 && <i className="fa-solid fa-right-long" />}
            </Fragment>
          );
        })}
      </div>

      {/* Tab description */}
      <div className="text-center mt-6 mb-12">
        {SELF_CHECK_TABS.map(
          (selfCheckTab) =>
            tab === selfCheckTab.name && (
              <Fragment key={selfCheckTab.name}>
                <div className="mb-2 text-lg font-semibold">{selfCheckTab.title}</div>
                <div className="px-4 text-sm text-semiblack/80 leading-relaxed">
                  {selfCheckTab.description}
                </div>
              </Fragment>
            )
        )}
      </div>

      {/* Actions */}
      {tab === 'OSS' && <SelfCheckOSS id={params.id} changed={changed} setChanged={setChanged} />}
      {tab === 'Package' && <SelfCheckPackage id={params.id} />}
      {tab === 'Notice' && <SelfCheckNotice id={params.id} />}
    </>
  );
}
