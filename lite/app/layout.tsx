import Providers from '@/components/providers';
import { sourceSansProFont } from '@/lib/fonts';
import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'FOSSLight Hub Lite'
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={sourceSansProFont.className}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
