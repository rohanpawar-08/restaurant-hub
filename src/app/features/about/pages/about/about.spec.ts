import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { About } from './about';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

describe('About', () => {
  let component: About;
  let fixture: ComponentFixture<About>;

  beforeEach(async () => {
    const mockSettingsService = {
      restaurantName: signal('Spice Hub'),
      tagline: signal('Authentic Indian Flavors'),
      phone: signal('9876543210'),
      email: signal('spicehub@example.com'),
      addressLine1: signal('45 Curry Lane'),
      addressLine2: signal('Near Metro Station'),
      city: signal('Pune'),
      state: signal('Maharashtra'),
      pinCode: signal('411001'),
      openingTime: signal('11:00 AM'),
      closingTime: signal('11:00 PM'),
      estimatedDeliveryMinutes: signal(30),
    };

  await TestBed.configureTestingModule({
      imports: [About],
      providers: [
        provideRouter([]),
        { provide: RestaurantSettingsService, useValue: mockSettingsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(About);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create the About component', () => {
    expect(component).toBeTruthy();
  });

  it('should dynamically render the restaurant name in header and story', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const title = compiled.querySelector('.about-title');
    expect(title?.textContent).toContain('Spice Hub');

    const story = compiled.querySelector('.story-text');
    expect(story?.textContent).toContain('Spice Hub');
  });

  it('should render all 6 core values cards', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('.value-card');
    expect(cards.length).toBe(6);
  });

  it('should display dynamic contact and location information', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('45 Curry Lane');
    expect(compiled.textContent).toContain('Pune');
    expect(compiled.textContent).toContain('9876543210');
    expect(compiled.textContent).toContain('11:00 AM - 11:00 PM');
  });

  it('should have CTA buttons linking to /menu and /contact', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const menuBtn = compiled.querySelector('a[routerLink="/menu"]');
    const contactBtn = compiled.querySelector('a[routerLink="/contact"]');
    expect(menuBtn).toBeTruthy();
    expect(contactBtn).toBeTruthy();
  });
});
