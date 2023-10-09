export const rootMenu = {
  name: 'Dashboard',
  icon: 'fa-solid fa-chart-line'
};

export const menus = [
  {
    name: 'Database',
    icon: 'fa-solid fa-database',
    path: '/database',
    sub: [
      { name: 'OSS', path: '/oss' },
      { name: 'License', path: '/license' },
      { name: 'Vulnerability', path: '/vulnerability' }
    ]
  },
  {
    name: 'Self-Check',
    icon: 'fa-solid fa-list-check',
    path: '/self-check',
    sub: [{ name: 'Project', path: '/project' }]
  },
  {
    name: 'Etc',
    icon: 'fa-solid fa-gear',
    path: '/etc',
    sub: [
      { name: 'User Management', path: '/user' },
      { name: 'Code Management', path: '/code' }
    ]
  }
];
