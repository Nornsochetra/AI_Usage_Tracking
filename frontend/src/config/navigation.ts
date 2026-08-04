export interface NavItem {
  title: string;
  href: string;
  icon?: string;
}

export const mainNavItems: NavItem[] = [
  { title: 'Dashboard', href: '/dashboard' },
  { title: 'Teams', href: '/teams' },
  { title: 'Users', href: '/users' },
  { title: 'API Keys', href: '/api-keys' },
  { title: 'Playground', href: '/playground' },
];
