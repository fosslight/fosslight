'use client';

import clsx from 'clsx';
import { useEffect, useRef, useState } from 'react';

export default function Modal({
  show,
  onHide,
  hideByBackdrop = true,
  children
}: {
  show: boolean;
  onHide: () => void;
  hideByBackdrop?: boolean;
  children: React.ReactNode;
}) {
  const [visible, setVisible] = useState(show);
  const [animate, setAnimate] = useState(show);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (show === visible) {
      return;
    }

    // Show the modal
    if (show) {
      // Create and the modal
      setVisible(true);

      // Animation on show
      setAnimate(true);
    }

    // Hide the modal
    else {
      // Remove the modal after its animation ends
      const backdrop = ref.current;
      backdrop?.addEventListener('transitionend', function handleTransitionEnd() {
        backdrop?.removeEventListener('transitionend', handleTransitionEnd);
        setVisible(false);
      });

      // Animation on hide
      setAnimate(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show]);

  return (
    <div
      className={clsx(
        'fixed inset-0 flex justify-center items-center bg-charcoal/50 transition-opacity duration-300 z-50',
        visible ? 'visible' : 'invisible',
        animate ? 'opacity-100' : 'opacity-0'
      )}
      onClick={() => {
        if (hideByBackdrop) {
          onHide();
        }
      }}
      ref={ref}
    >
      <div
        className={clsx(
          'p-6 rounded-lg bg-white shadow-[0px_0px_4px_4px_rgba(0,0,0,0.2)] transition-[transform,opacity] duration-300',
          animate ? 'translate-y-0 opacity-100' : '-translate-y-4 opacity-0'
        )}
        onClick={(e) => e.stopPropagation()}
      >
        {children}
      </div>
    </div>
  );
}
