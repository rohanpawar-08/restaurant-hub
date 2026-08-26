import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { Hero } from './hero';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

describe('Hero', () => {
  let component: Hero;
  let fixture: ComponentFixture<Hero>;

  beforeEach(async () => {
    const mockSettingsService = {
      restaurantName: signal('RestaurantHub'),
      tagline: signal('Fresh food, delivered with care'),
      heroImageUrl: signal(null),
    };

    await TestBed.configureTestingModule({
      imports: [Hero],
      providers: [
        provideRouter([]),
        { provide: RestaurantSettingsService, useValue: mockSettingsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Hero);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
