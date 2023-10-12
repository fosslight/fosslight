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
