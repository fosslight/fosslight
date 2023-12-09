import clsx from 'clsx';
import { useEffect, useRef, useState } from 'react';

export default function Modal({
  show,
  onHide,
  size,
  hideByBackdrop = true,
  hideScrollbar = true,
  children
}: {
  show: boolean;
  onHide: () => void;
  size: 'sm' | 'md' | 'lg';
  hideByBackdrop?: boolean;
  hideScrollbar?: boolean;
  children: React.ReactNode;
}) {
  const [visible, setVisible] = useState(show);
  const [animate, setAnimate] = useState(show);
  const ref = useRef<HTMLDivElement>(null);

  let width = '';
  if (size === 'sm') width = 'w-[400px]';
  else if (size === 'md') width = 'w-[700px]';
  else if (size === 'lg') width = 'w-[1000px]';

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

  // Hide the modal by ESC
  useEffect(() => {
    function handleEsc(e: KeyboardEvent) {
      if (visible && hideByBackdrop && e.code === 'Escape') {
        onHide();
      }
    }

    document.addEventListener('keydown', handleEsc);
    return () => {
      document.removeEventListener('keydown', handleEsc);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

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
          'max-w-[calc(100%-24px)] max-h-[calc(100%-24px)] p-6 rounded-lg bg-white shadow-[0px_0px_4px_4px_rgba(0,0,0,0.2)] overflow-y-auto transition-[transform,opacity] duration-300',
          width,
          animate ? 'translate-y-0 opacity-100' : '-translate-y-4 opacity-0',
          hideScrollbar && 'no-scrollbar'
        )}
        onClick={(e) => e.stopPropagation()}
      >
        {children}
      </div>
    </div>
  );
}
