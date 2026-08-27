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
        title: 'RestaurantHub | Operations Dashboard',
        loadComponent: () =>
          import('./features/admin/pages/dashboard/dashboard').then(
            (m) => m.Dashboard
          ),
      },
      {
        path: 'orders',
        title: 'RestaurantHub | Admin Orders',
        loadComponent: () =>
          import('./features/admin/pages/admin-orders/admin-orders').then(
            (m) => m.AdminOrders
          ),
      },
      {
        path: 'categories',
        title: 'RestaurantHub | Admin Categories',
        loadComponent: () =>
          import('./features/admin/pages/admin-categories/admin-categories').then(
            (m) => m.AdminCategories
          ),
      },
      {
        path: 'menu',
        title: 'RestaurantHub | Admin Food Menu',
        loadComponent: () =>
          import('./features/admin/pages/admin-menu/admin-menu').then(
            (m) => m.AdminMenu
          ),
      },
      {
        path: 'settings',
        title: 'RestaurantHub | Restaurant Settings',
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
        title: 'RestaurantHub | Online Ordering',
        component: Home,
      },
      {
        path: 'menu',
        title: 'RestaurantHub | Menu',
        loadComponent: () =>
          import('./features/menu/pages/menu/menu').then((m) => m.Menu),
      },
      {
        path: 'cart',
        title: 'RestaurantHub | Cart',
        loadComponent: () =>
          import('./features/cart/pages/cart/cart').then((m) => m.Cart),
      },
      {
        path: 'checkout',
        title: 'RestaurantHub | Checkout',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/checkout/pages/checkout/checkout').then(
            (m) => m.Checkout
          ),
      },
      {
        path: 'order-success',
        title: 'RestaurantHub | Order Confirmed',
        loadComponent: () =>
          import('./features/checkout/pages/order-success/order-success').then(
            (m) => m.OrderSuccess
          ),
      },
      {
        path: 'orders',
        title: 'RestaurantHub | My Orders',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/orders/pages/orders/orders').then((m) => m.Orders),
      },
      {
        path: 'login',
        title: 'RestaurantHub | Login',
        loadComponent: () =>
          import('./features/auth/pages/login/login').then((m) => m.Login),
      },
      {
        path: 'register',
        title: 'RestaurantHub | Register',
        loadComponent: () =>
          import('./features/auth/pages/register/register').then(
            (m) => m.Register
          ),
      },
      {
        path: 'about',
        title: 'RestaurantHub | About Us',
        loadComponent: () =>
          import('./features/about/pages/about/about').then((m) => m.About),
      },
      {
        path: 'contact',
        title: 'RestaurantHub | Contact Us',
        loadComponent: () =>
          import('./features/contact/pages/contact/contact').then(
            (m) => m.Contact
          ),
      },
      {
        path: 'profile',
        title: 'RestaurantHub | My Profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/profile/pages/profile/profile').then(
            (m) => m.Profile
          ),
      },
      {
        path: '**',
        title: 'RestaurantHub | Page Not Found',
        loadComponent: () =>
          import('./features/not-found/pages/not-found/not-found').then(
            (m) => m.NotFound
          ),
      },
    ],
  },
];