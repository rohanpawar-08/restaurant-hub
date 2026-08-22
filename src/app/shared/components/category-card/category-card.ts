import { Component, input } from '@angular/core';
import { FoodCategory } from '../../models/category.model';

@Component({
  selector: 'app-category-card',
  standalone: true,
  imports: [],
  templateUrl: './category-card.html',
  styleUrl: './category-card.scss',
})
export class CategoryCard {
  readonly category = input.required<FoodCategory>();
}
