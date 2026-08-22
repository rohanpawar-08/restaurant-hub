import { Routes } from '@angular/router';

import { PublicLayout } from './core/layouts/public-layout/public-layout';
import { Home } from './features/home/pages/home/home';
import { authGuard } from './core/guards/auth.guard';

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
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/checkout/pages/checkout/checkout').then(
            (m) => m.Checkout
          ),
      },
      {
        path: 'order-success',
        loadComponent: () =>
          import('./features/checkout/pages/order-success/order-success').then(
            (m) => m.OrderSuccess
          ),
      },
      {
        path: 'orders',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/orders/pages/orders/orders').then((m) => m.Orders),
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/pages/login/login').then((m) => m.Login),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/pages/register/register').then(
            (m) => m.Register
          ),
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/profile/pages/profile/profile').then(
            (m) => m.Profile
          ),
      },
    ],
  },
];