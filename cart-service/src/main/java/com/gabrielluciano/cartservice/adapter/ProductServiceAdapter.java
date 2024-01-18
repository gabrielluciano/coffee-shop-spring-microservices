package com.gabrielluciano.cartservice.adapter;

import com.gabrielluciano.cartservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceAdapter implements ProductService {


    private final WebClient webClient;

    @Override
    public boolean productExists(Long productId) {
        return Boolean.TRUE.equals(webClient.get()
                .uri("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> Mono.just(response.statusCode().equals(HttpStatus.OK)))
                .block());
    }
}
