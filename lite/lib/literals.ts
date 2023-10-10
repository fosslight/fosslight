export const rootMenu = {
  name: 'Dashboard',
  short: 'Home',
  icon: 'fa-solid fa-chart-line'
};

export const menus = [
  {
    name: 'Project',
    short: 'Project',
    icon: 'fa-solid fa-list-check',
    path: '/project'
  },
  {
    name: 'Database',
    short: 'DB',
    icon: 'fa-solid fa-database',
    path: '/database',
    sub: [
      { name: 'OSS', path: '/oss' },
      { name: 'License', path: '/license' },
      { name: 'Vulnerability', path: '/vulnerability' }
    ]
  },
  {
    name: 'Etc',
    short: 'Etc',
    icon: 'fa-solid fa-gear',
    path: '/etc',
    sub: [
      { name: 'User Management', path: '/user' },
      { name: 'Code Management', path: '/code' }
    ]
  }
];
