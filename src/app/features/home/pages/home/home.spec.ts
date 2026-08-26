import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { Home } from './home';
import { environment } from '../../../../../environments/environment';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    const mockSettingsService = {
      restaurantName: signal('RestaurantHub'),
      tagline: signal('Fresh food, delivered with care'),
      heroImageUrl: signal(null),
      primaryColor: signal('#FF6B00'),
      secondaryColor: signal('#1E293B'),
    };

    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: RestaurantSettingsService, useValue: mockSettingsService },
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;

    // Handle background menu requests triggered by child components
    httpMock.expectOne(`${environment.apiBaseUrl}/categories?activeOnly=true`).flush([]);
    httpMock.expectOne(`${environment.apiBaseUrl}/foods?activeOnly=true`).flush([]);

    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
