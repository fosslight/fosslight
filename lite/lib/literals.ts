export const rootMenu = {
  name: 'Dashboard',
  icon: 'fa-solid fa-chart-line'
};

export const menus = [
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

export const selfCheckTabs: SelfCheckTab[] = [
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
