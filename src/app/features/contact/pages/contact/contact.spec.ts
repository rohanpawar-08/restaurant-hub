import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { Contact } from './contact';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

describe('Contact', () => {
  let component: Contact;
  let fixture: ComponentFixture<Contact>;

  beforeEach(async () => {
    const mockSettingsService = {
      restaurantName: signal('Curry Delight'),
      tagline: signal('Flavors of India'),
      phone: signal('9123456789'),
      email: signal('info@currydelight.com'),
      addressLine1: signal('77 Spice Market'),
      addressLine2: signal('Near Heritage Tower'),
      city: signal('Bengaluru'),
      state: signal('Karnataka'),
      pinCode: signal('560001'),
      openingTime: signal('10:30 AM'),
      closingTime: signal('10:30 PM'),
      gstin: signal('29ABCDE1234F1Z5'),
      fssaiNumber: signal('11223344556677'),
    };

    await TestBed.configureTestingModule({
      imports: [Contact],
      providers: [
        provideRouter([]),
        { provide: RestaurantSettingsService, useValue: mockSettingsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Contact);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create the Contact component', () => {
    expect(component).toBeTruthy();
  });

  it('should render dynamic restaurant settings (name, phone, email, address, hours)', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Curry Delight');
    expect(compiled.textContent).toContain('9123456789');
    expect(compiled.textContent).toContain('info@currydelight.com');
    expect(compiled.textContent).toContain('77 Spice Market');
    expect(compiled.textContent).toContain('Bengaluru, Karnataka - 560001');
    expect(compiled.textContent).toContain('10:30 AM - 10:30 PM');
    expect(compiled.textContent).toContain('29ABCDE1234F1Z5');
    expect(compiled.textContent).toContain('11223344556677');
  });

  it('should render functional tel: and mailto: links', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const phoneLink = compiled.querySelector('#contact-phone-link') as HTMLAnchorElement;
    const emailLink = compiled.querySelector('#contact-email-link') as HTMLAnchorElement;

    expect(phoneLink).toBeTruthy();
    expect(phoneLink.href).toContain('tel:+919123456789');

    expect(emailLink).toBeTruthy();
    expect(emailLink.href).toContain('mailto:info@currydelight.com');
  });

  it('should validate form and show error for invalid submissions', () => {
    component.onSubmit();
    fixture.detectChanges();

    expect(component.contactForm.valid).toBe(false);
    expect(component.formSubmitted()).toBe(false);
  });

  it('should allow valid form submission and show confirmation banner', () => {
    component.contactForm.patchValue({
      name: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      subject: 'General Inquiry',
      message: 'Can I book a table for 4 this Saturday?',
    });

    component.onSubmit();
    fixture.detectChanges();

    expect(component.contactForm.valid).toBe(true);
    expect(component.formSubmitted()).toBe(true);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.form-success-banner')).toBeTruthy();
  });
});
