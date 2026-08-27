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

  it('should render fallback image when heroImageUrl is null or empty', () => {
    fixture.detectChanges();
    const img: HTMLImageElement = fixture.nativeElement.querySelector('.hero-img');
    expect(img).toBeTruthy();
    expect(img.src).toContain('assets/images/hero/hero-food.png');
  });

  it('should render configured hero URL when present', async () => {
    const settingsService = TestBed.inject(RestaurantSettingsService) as any;
    settingsService.heroImageUrl.set('https://cdn.example.com/custom-hero.jpg');
    fixture.detectChanges();
    await fixture.whenStable();

    const img: HTMLImageElement = fixture.nativeElement.querySelector('.hero-img');
    expect(img.src).toBe('https://cdn.example.com/custom-hero.jpg');
  });

  it('should switch to fallback image when configured hero image fails to load', async () => {
    const settingsService = TestBed.inject(RestaurantSettingsService) as any;
    settingsService.heroImageUrl.set('https://cdn.example.com/broken-hero.jpg');
    fixture.detectChanges();
    await fixture.whenStable();

    let img: HTMLImageElement = fixture.nativeElement.querySelector('.hero-img');
    expect(img.src).toBe('https://cdn.example.com/broken-hero.jpg');

    // Simulate image error event
    img.dispatchEvent(new Event('error'));
    fixture.detectChanges();
    await fixture.whenStable();

    img = fixture.nativeElement.querySelector('.hero-img');
    expect(img.src).toContain('assets/images/hero/hero-food.png');
  });
});

