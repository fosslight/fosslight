export const rootMenu = {
  name: 'Dashboard',
  icon: 'fa-solid fa-chart-line'
};

export const menus = [
  {
    name: 'Project',
    icon: 'fa-solid fa-list-check',
    path: '/project'
  },
  {
    name: 'Database',
    icon: 'fa-solid fa-database',
    path: '/database',
    sub: [
      { name: 'OSS', path: '/oss' },
      { name: 'License', path: '/license' },
      { name: 'Vulnerability', path: '/vulnerability' }
    ]
  }
];
