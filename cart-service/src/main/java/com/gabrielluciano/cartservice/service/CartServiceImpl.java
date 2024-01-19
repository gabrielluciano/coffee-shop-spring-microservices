package com.gabrielluciano.cartservice.service;

import com.gabrielluciano.cartservice.dto.CartRequest;
import com.gabrielluciano.cartservice.dto.CartResponse;
import com.gabrielluciano.cartservice.exception.CartNotFoundException;
import com.gabrielluciano.cartservice.exception.ProductNotFoundException;
import com.gabrielluciano.cartservice.model.Cart;
import com.gabrielluciano.cartservice.model.CartItem;
import com.gabrielluciano.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public CartResponse addItem(CartRequest cartRequest) {
        Long productId = cartRequest.getProductId();
        if (productDoesNotExist(productId))
            throw new ProductNotFoundException(productId);

        Cart savedCart = createOrUpdateCartFromCartRequest(cartRequest);
        return CartResponse.fromCart(savedCart);
    }

    @Override
    public CartResponse getCart(Long userId) {
        Optional<Cart> optionalCart = findCartByUserId(userId);
        return optionalCart
                .map(CartResponse::fromCart)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    @Override
    public void clearCart(Long userId) {
        findCartByUserId(userId).ifPresent(cart -> {
            cart.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));
            cartRepository.save(cart);
        });
    }

    private boolean productDoesNotExist(Long productId) {
        return !productService.productExists(productId);
    }

    private Cart createOrUpdateCartFromCartRequest(CartRequest cartRequest) {
        // Check if a cart already exists for the user; update if exists, otherwise create a new cart
        Optional<Cart> optionalCart = findCartByUserId(cartRequest.getUserId());
        return optionalCart
                .map(cart -> updateAndSaveCart(cart, cartRequest))
                .orElseGet(() -> createAndSaveCart(cartRequest));
    }

    private Optional<Cart> findCartByUserId(Long userId) {
        return cartRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    private Cart updateAndSaveCart(Cart cart, CartRequest cartRequest) {
        Cart updatedCart = updateCartWithCartRequest(cart, cartRequest);
        cartRepository.save(updatedCart);
        return updatedCart;
    }

    private Cart createAndSaveCart(CartRequest cartRequest) {
        Cart newCart = createCartFromCartRequest(cartRequest);
        cartRepository.save(newCart);
        return newCart;
    }

    private Cart updateCartWithCartRequest(Cart cart, CartRequest cartRequest) {
        // If item is already in the cart, increase its quantity, otherwise add the new item to cart
        Optional<CartItem> optionalCartItem = findCartItemInCart(cart, cartRequest.getProductId());
        if (optionalCartItem.isPresent()) {
            increaseCartItemQuantity(optionalCartItem.get(), cartRequest.getQuantity());
        } else {
            createCartItemFromCartRequestAndAddToCart(cartRequest, cart);
        }
        return cart;
    }

    private Cart createCartFromCartRequest(CartRequest cartRequest) {
        CartItem cartItem = createCartItemFromCartRequest(cartRequest);

        return Cart.builder()
                .userId(cartRequest.getUserId())
                .items(List.of(cartItem))
                .build();
    }

    private Optional<CartItem> findCartItemInCart(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private void increaseCartItemQuantity(CartItem cartItem, Integer quantity) {
        cartItem.increaseQuantityBy(quantity);
    }

    private void createCartItemFromCartRequestAndAddToCart(CartRequest cartRequest, Cart cart) {
        CartItem cartItem = createCartItemFromCartRequest(cartRequest);
        cart.addItem(cartItem);
    }

    private CartItem createCartItemFromCartRequest(CartRequest cartRequest) {
        return CartItem.fromProductIdAndQuantity(cartRequest.getProductId(),
                cartRequest.getQuantity());
    }
}
