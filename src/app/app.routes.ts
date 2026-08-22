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
      {
        path: 'cart',
        loadComponent: () =>
          import('./features/cart/pages/cart/cart').then((m) => m.Cart),
      },
      {
        path: 'checkout',
        loadComponent: () =>
          import('./features/checkout/pages/checkout/checkout').then((m) => m.Checkout),
      },
      {
        path: 'order-success',
        loadComponent: () =>
          import('./features/checkout/pages/order-success/order-success').then(
            (m) => m.OrderSuccess
          ),
      },
    ],
  },
];