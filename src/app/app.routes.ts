import { Routes } from '@angular/router';

import { PublicLayout } from './core/layouts/public-layout/public-layout';
import { AdminLayout } from './core/layouts/admin-layout/admin-layout';
import { Home } from './features/home/pages/home/home';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [adminGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/admin/pages/dashboard/dashboard').then(
            (m) => m.Dashboard
          ),
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/admin/pages/admin-orders/admin-orders').then(
            (m) => m.AdminOrders
          ),
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/admin/pages/admin-categories/admin-categories').then(
            (m) => m.AdminCategories
          ),
      },
      {
        path: 'menu',
        loadComponent: () =>
          import('./features/admin/pages/admin-menu/admin-menu').then(
            (m) => m.AdminMenu
          ),
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/admin/pages/admin-settings/admin-settings').then(
            (m) => m.AdminSettings
          ),
      },
    ],
  },
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
        path: 'about',
        loadComponent: () =>
          import('./features/about/pages/about/about').then((m) => m.About),
      },
      {
        path: 'contact',
        loadComponent: () =>
          import('./features/contact/pages/contact/contact').then(
            (m) => m.Contact
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