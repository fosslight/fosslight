export const rootMenu: Nav.RootMenu = {
  name: 'Dashboard',
  icon: 'fa-solid fa-chart-line'
};

export const menus: Nav.Menu[] = [
  {
    name: 'Self-Check',
    icon: 'fa-solid fa-list-check',
    path: '/self-check'
  },
  {
    name: 'Database',
    icon: 'fa-solid fa-server',
    path: '/database',
    sub: [
      { name: 'OSS', path: '/oss' },
      { name: 'License', path: '/license' },
      { name: 'Vulnerability', path: '/vulnerability' }
    ]
  }
];

export const selfCheckTabs: SelfCheck.Tab[] = [
  {
    name: 'OSS',
    title: 'Listing up your OSS',
    description:
      'List up the OSS in your project and check the details(licenses, obligations, restrictions, and vulnerabilities).'
  },
  {
    name: 'Package',
    title: 'Upload your packages',
    description: 'Upload the package files containing the source codes that must be disclosed.'
  },
  {
    name: 'Notice',
    title: 'Generate a notice',
    description:
      'Generate a notice document explaining the OSS in your project, as a format that you want.'
  }
];

export const ossTypes: Detail.OSSTypes = {
  M: {
    name: 'Multi License',
    desc: 'It contains source codes under multiple licenses. (EX. lib is LGPL-2.1 and src is GPL-2.0)'
  },
  D: {
    name: 'Dual License',
    desc: 'You can select one of the registered licenses. (EX. GPL-2.0 or MIT)'
  },
  V: {
    name: 'Version Different License',
    desc: 'It is distributed under different licenses according to its versions. (EX. 1.0 is GPL-2.0, but 2.0 is BSD-3-Clause)'
  }
};
