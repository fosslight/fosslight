import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './lib/**/*.{js,ts,jsx,tsx,mdx}'
  ],
  theme: {
    extend: {
      colors: {
        charcoal: '#34393f',
        semicharcoal: '#68727e',
        crimson: '#b02a42',
        semiwhite: '#eeeeee',
        semiblack: '#222222',
        gray: '#bbbbbb',
        semigray: '#cccccc',
        darkgray: '#aaaaaa'
      }
    }
  },
  plugins: []
};
export default config;
