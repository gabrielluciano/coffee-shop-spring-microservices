package com.gabrielluciano.cartservice.adapter;

import com.gabrielluciano.cartservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceAdapter implements ProductService {

    @LoadBalanced
    private final WebClient.Builder webClientBuilder;

    private static final String API_URL = "http://product-service/api/v1/products/";

    @Override
    public boolean productExists(Long productId) {
        return Boolean.TRUE.equals(webClientBuilder.build().get()
                .uri(API_URL + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> Mono.just(response.statusCode().equals(HttpStatus.OK)))
                .block());
    }
}
