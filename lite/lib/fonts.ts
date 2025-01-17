import localFont from 'next/font/local';

export const sourceSansProFont = localFont({
  src: [
    { path: '../public/fonts/SourceSansPro-Light.ttf', weight: '300' },
    { path: '../public/fonts/SourceSansPro-Regular.ttf', weight: '400' },
    { path: '../public/fonts/SourceSansPro-Semibold.ttf', weight: '600' },
    { path: '../public/fonts/SourceSansPro-Bold.ttf', weight: '700' },
    { path: '../public/fonts/SourceSansPro-Black.ttf', weight: '900' }
  ]
});
