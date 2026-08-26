import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FoodCard } from './food-card';
import { CartService } from '../../../core/services/cart.service';
import { Food } from '../../models/food.model';

describe('FoodCard Component', () => {
  let component: FoodCard;
  let fixture: ComponentFixture<FoodCard>;
  let cartService: CartService;

  const mockAvailableFood: Food = {
    id: '1',
    name: 'Margherita Pizza',
    description: 'Fresh basil and mozzarella',
    category: 'Pizza',
    categorySlug: 'pizza',
    price: 299,
    rating: 4.8,
    image: 'pizza.jpg',
    isVeg: true,
    isPopular: true,
    isAvailable: true,
  };

  const mockUnavailableFood: Food = {
    id: '2',
    name: 'Truffle Pasta',
    description: 'Creamy pasta with black truffles',
    category: 'Pasta',
    categorySlug: 'pasta',
    price: 450,
    rating: 4.9,
    image: 'pasta.jpg',
    isVeg: true,
    isPopular: false,
    isAvailable: false,
  };

  const mockNewUnratedFood: Food = {
    id: '3',
    name: 'Avocado Toast',
    description: 'Fresh artisanal sourdough with avocado',
    category: 'Breakfast',
    categorySlug: 'breakfast',
    price: 180,
    rating: 0.0,
    image: 'toast.jpg',
    isVeg: true,
    isPopular: false,
    isAvailable: true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FoodCard],
      providers: [CartService],
    }).compileComponents();

    cartService = TestBed.inject(CartService);
  });

  it('should create and render available food item with Add to Cart button enabled', () => {
    fixture = TestBed.createComponent(FoodCard);
    fixture.componentRef.setInput('food', mockAvailableFood);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Margherita Pizza');
    expect(compiled.textContent).toContain('Add to Cart');

    const addBtn = compiled.querySelector('.add-to-cart-btn') as HTMLButtonElement;
    expect(addBtn.disabled).toBe(false);
  });

  it('should render unavailable food item with Sold Out badge and disabled button', () => {
    fixture = TestBed.createComponent(FoodCard);
    fixture.componentRef.setInput('food', mockUnavailableFood);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Sold Out');
    expect(compiled.textContent).toContain('Unavailable');

    const unavailBtn = compiled.querySelector('.btn-unavailable') as HTMLButtonElement;
    expect(unavailBtn).toBeTruthy();
    expect(unavailBtn.disabled).toBe(true);
  });

  it('should render "New" badge for unrated food (rating <= 0) without displaying 0.0 stars', () => {
    fixture = TestBed.createComponent(FoodCard);
    fixture.componentRef.setInput('food', mockNewUnratedFood);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('New');
    expect(compiled.textContent).not.toContain('0.0');
    expect(compiled.querySelector('.rating-badge-new')).toBeTruthy();
  });

  it('should add item to cart on clicking Add to Cart for available food', () => {
    const addSpy = vi.spyOn(cartService, 'addToCart');

    fixture = TestBed.createComponent(FoodCard);
    fixture.componentRef.setInput('food', mockAvailableFood);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const addBtn = fixture.nativeElement.querySelector('.add-to-cart-btn') as HTMLButtonElement;
    addBtn.click();

    expect(addSpy).toHaveBeenCalledWith(mockAvailableFood);
    expect(component.isAdded()).toBe(true);
  });

  it('should render uploaded /media/ image correctly and fallback to placeholder on image error', () => {
    const mediaFood: Food = {
      ...mockAvailableFood,
      image: '/media/food/5c64b94c-7abd.jpg',
      icon: '🍕',
    };

    fixture = TestBed.createComponent(FoodCard);
    fixture.componentRef.setInput('food', mediaFood);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const img = compiled.querySelector('.food-img') as HTMLImageElement;
    expect(img).toBeTruthy();
    expect(img.getAttribute('src')).toBe('/media/food/5c64b94c-7abd.jpg');

    // Simulate broken image error
    component.onImageError();
    fixture.detectChanges();

    expect(compiled.querySelector('.food-img')).toBeFalsy();
    expect(compiled.querySelector('.image-placeholder')).toBeTruthy();
    expect(compiled.textContent).toContain('🍕');
  });
});
