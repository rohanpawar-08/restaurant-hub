import { Component } from '@angular/core';
import { Hero } from '../../components/hero/hero';
import { Categories } from '../../components/categories/categories';
import { PopularDishes } from '../../components/popular-dishes/popular-dishes';
import { WhyChooseUs } from '../../components/why-choose-us/why-choose-us';
import { Testimonials } from '../../components/testimonials/testimonials';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [Hero, Categories, PopularDishes, WhyChooseUs, Testimonials],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {}