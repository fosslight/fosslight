import Providers from '@/components/providers';
import { sourceSansProFont } from '@/lib/fonts';
import clsx from 'clsx';
import type { Metadata } from 'next';
import Script from 'next/script';
import './globals.css';

export const metadata: Metadata = {
  title: 'FOSSLight Hub Lite'
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={clsx('text-semiblack', sourceSansProFont.className)}>
        <Providers>{children}</Providers>
      </body>
      <Script src="https://kit.fontawesome.com/b3681c51ce.js" />
    </html>
  );
}
