package com.restauranthub.order;

import com.restauranthub.food.Food;
import com.restauranthub.food.FoodRepository;
import com.restauranthub.food.exception.FoodNotFoundException;
import com.restauranthub.order.dto.CreateOrderRequest;
import com.restauranthub.order.dto.OrderItemRequest;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.exception.FoodUnavailableException;
import com.restauranthub.order.exception.OrderNotFoundException;
import com.restauranthub.order.exception.OrdersClosedException;
import com.restauranthub.settings.RestaurantSettings;
import com.restauranthub.settings.RestaurantSettingsService;
import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enterprise Service governing customer order processing and lifecycle management.
 *
 * Security & Financial Guarantees:
 * 1. Server-Authoritative Pricing: Disregards any client-supplied totals/prices.
 *    Current prices are loaded directly from MySQL, and totals are computed with BigDecimal.
 * 2. Dynamic Restaurant Policy: Delivery fee, free delivery threshold, and ordering status
 *    are driven dynamically by active RestaurantSettings.
 * 3. Atomic Transaction Management: Order creation and item snapshot saves occur in a single transaction.
 * 4. Customer Ownership Isolation: Users can only query orders associated with their authenticated ID.
 */
@Service
public class OrderService {

    public static final BigDecimal DEFAULT_FREE_DELIVERY_THRESHOLD = new BigDecimal("500.00");
    public static final BigDecimal DEFAULT_STANDARD_DELIVERY_FEE = new BigDecimal("40.00");
    public static final int DEFAULT_ESTIMATED_DELIVERY_MINUTES = 35;

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final RestaurantSettingsService settingsService;

    public OrderService(
            OrderRepository orderRepository,
            FoodRepository foodRepository,
            UserRepository userRepository,
            RestaurantSettingsService settingsService
    ) {
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
    }

    /**
     * Creates a new customer order with server-calculated financial totals and snapshots.
     *
     * @param request   validated order creation payload containing items and delivery info
     * @param userEmail email of the currently authenticated principal
     * @return safe OrderResponse
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {
        User user = resolveAuthenticatedUser(userEmail);

        RestaurantSettings settings = settingsService.getActiveSettings();
        if (Boolean.FALSE.equals(settings.getAcceptingOrders())) {
            throw new OrdersClosedException();
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        if (request.paymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        if (request.paymentMethod() != PaymentMethod.COD) {
            throw new com.restauranthub.order.exception.UnsupportedPaymentMethodException(
                    "Online payment is not available yet. Please choose Cash on Delivery."
            );
        }

        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentMethod(request.paymentMethod());
        order.setCustomerName(request.customerName().trim());
        order.setCustomerEmail(request.customerEmail().trim().toLowerCase());
        order.setCustomerPhone(request.customerPhone().trim());
        order.setAddressLine1(request.addressLine1().trim());
        order.setAddressLine2(request.addressLine2() != null ? request.addressLine2().trim() : null);
        order.setCity(request.city().trim());
        order.setState(request.state().trim());
        order.setPostalCode(request.postalCode().trim());
        order.setDeliveryInstructions(request.deliveryInstructions() != null ? request.deliveryInstructions().trim() : null);

        int estimatedDeliveryMinutes = settings.getEstimatedDeliveryMinutes() != null
                ? settings.getEstimatedDeliveryMinutes()
                : DEFAULT_ESTIMATED_DELIVERY_MINUTES;
        order.setEstimatedDeliveryMinutes(estimatedDeliveryMinutes);

        for (OrderItemRequest itemReq : request.items()) {
            if (itemReq.quantity() == null || itemReq.quantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be at least 1.");
            }

            Food food = foodRepository.findById(itemReq.foodId())
                    .orElseThrow(() -> new FoodNotFoundException(itemReq.foodId()));

            if (Boolean.FALSE.equals(food.getAvailable())) {
                throw new FoodUnavailableException(food.getId(), food.getName());
            }

            BigDecimal unitPrice = food.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = new OrderItem(
                    order,
                    food,
                    food.getName(),
                    unitPrice,
                    itemReq.quantity(),
                    lineTotal
            );

            order.addItem(orderItem);
        }

        BigDecimal deliveryFee = calculateDeliveryFee(subtotal, settings);
        BigDecimal total = subtotal.add(deliveryFee).setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Retrieves the order history for the authenticated customer, ordered newest first.
     *
     * @param userEmail email of the currently authenticated principal
     * @return list of orders belonging to the customer
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(String userEmail) {
        User user = resolveAuthenticatedUser(userEmail);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream().map(OrderResponse::fromEntity).toList();
    }

    /**
     * Retrieves a single order by ID belonging specifically to the authenticated customer.
     *
     * @param orderId   order primary key
     * @param userEmail email of the currently authenticated principal
     * @return OrderResponse if owned by the customer
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String userEmail) {
        User user = resolveAuthenticatedUser(userEmail);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Calculates the delivery fee based on active RestaurantSettings.
     * Rule: Free delivery if subtotal >= freeDeliveryThreshold, otherwise deliveryFee.
     *
     * @param subtotal calculated items subtotal
     * @param settings active restaurant configuration
     * @return delivery fee as BigDecimal
     */
    public BigDecimal calculateDeliveryFee(BigDecimal subtotal, RestaurantSettings settings) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal threshold = settings != null && settings.getFreeDeliveryThreshold() != null
                ? settings.getFreeDeliveryThreshold()
                : DEFAULT_FREE_DELIVERY_THRESHOLD;

        BigDecimal fee = settings != null && settings.getDeliveryFee() != null
                ? settings.getDeliveryFee()
                : DEFAULT_STANDARD_DELIVERY_FEE;

        if (subtotal.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Overload for delivery fee calculation using current active settings.
     *
     * @param subtotal calculated items subtotal
     * @return delivery fee as BigDecimal
     */
    public BigDecimal calculateDeliveryFee(BigDecimal subtotal) {
        RestaurantSettings settings = settingsService != null ? settingsService.getActiveSettings() : null;
        return calculateDeliveryFee(subtotal, settings);
    }

    private User resolveAuthenticatedUser(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadCredentialsException("User is not authenticated.");
        }
        return userRepository.findByEmailIgnoreCase(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Authenticated user not found in database."));
    }
}

