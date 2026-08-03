import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Navbar } from '../../../shared/components/navbar/navbar';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [Navbar, RouterOutlet],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.scss',
})
export class PublicLayout {}
