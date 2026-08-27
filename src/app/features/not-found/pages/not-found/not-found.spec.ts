import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { NotFound } from './not-found';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

describe('NotFound', () => {
  let component: NotFound;
  let fixture: ComponentFixture<NotFound>;

  beforeEach(async () => {
    const mockSettingsService = {
      restaurantName: signal('Spice Hub'),
    };

    await TestBed.configureTestingModule({
      imports: [NotFound],
      providers: [
        provideRouter([]),
        { provide: RestaurantSettingsService, useValue: mockSettingsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotFound);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create the NotFound component', () => {
    expect(component).toBeTruthy();
  });

  it('should display Page Not Found and 404 message', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.not-found-title')?.textContent).toBe('Page Not Found');
    expect(compiled.querySelector('.error-badge')?.textContent).toContain('404');
  });

  it('should have navigation buttons linking to / and /menu', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const homeLink = compiled.querySelector('a[routerLink="/"]');
    const menuLink = compiled.querySelector('a[routerLink="/menu"]');

    expect(homeLink).toBeTruthy();
    expect(menuLink).toBeTruthy();
  });
});
