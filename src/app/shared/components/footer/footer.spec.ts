import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { Footer } from './footer';
import { RestaurantSettingsService } from '../../../core/services/restaurant-settings.service';

describe('Footer', () => {
  let component: Footer;
  let fixture: ComponentFixture<Footer>;

  beforeEach(async () => {
    const mockSettingsService = {
      restaurantName: signal('RestaurantHub India'),
      tagline: signal('Artisan Cuisine'),
      phone: signal('9876543210'),
      email: signal('hello@restauranthub.com'),
      addressLine1: signal('123 Gourmet St'),
      addressLine2: signal('Bandra West'),
      city: signal('Mumbai'),
      state: signal('Maharashtra'),
      pinCode: signal('400050'),
      gstin: signal('27ABCDE1234F1Z5'),
      fssaiNumber: signal('11521000000001'),
      openingTime: signal('10:00:00'),
      closingTime: signal('23:00:00'),
    };

    await TestBed.configureTestingModule({
      imports: [Footer],
      providers: [
        provideRouter([]),
        { provide: RestaurantSettingsService, useValue: mockSettingsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Footer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and render dynamic restaurant brand and contact details', () => {
    expect(component).toBeTruthy();
    expect(component.formattedAddress()).toContain('123 Gourmet St');
    expect(component.formattedAddress()).toContain('Mumbai');
    expect(component.formattedHours()).toBe('Daily: 10:00:00 - 23:00:00');

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('RestaurantHub India');
    expect(compiled.textContent).toContain('9876543210');
    expect(compiled.textContent).toContain('27ABCDE1234F1Z5');
    expect(compiled.textContent).toContain('11521000000001');
  });
});
