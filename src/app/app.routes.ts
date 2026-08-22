import { Routes } from '@angular/router';

import { PublicLayout } from './core/layouts/public-layout/public-layout';
import { Home } from './features/home/pages/home/home';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        component: Home,
      },
      {
        path: 'menu',
        loadComponent: () =>
          import('./features/menu/pages/menu/menu').then((m) => m.Menu),
      },
    ],
  },
];