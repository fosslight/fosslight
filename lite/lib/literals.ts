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
      { name: 'OSS', short: 'OSS', path: '/oss' },
      { name: 'License', short: 'License', path: '/license' },
      { name: 'Vulnerability', short: 'Vunl', path: '/vulnerability' }
    ]
  }
];
