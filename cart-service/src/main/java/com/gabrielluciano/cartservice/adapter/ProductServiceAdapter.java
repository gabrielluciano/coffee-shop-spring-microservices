package com.gabrielluciano.cartservice.adapter;

import com.gabrielluciano.cartservice.exception.ServiceUnavailableException;
import com.gabrielluciano.cartservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceAdapter implements ProductService {

    @LoadBalanced
    private final WebClient.Builder webClientBuilder;
    private final ReactiveCircuitBreakerFactory cbFactory;

    private static final String API_URL = "http://product-service/api/v1/products/";

    @Override
    public boolean productExists(Long productId) {
        return Boolean.TRUE.equals(cbFactory.create("product-service")
                .run(webClientCall(productId), throwable -> {
                    log.error("Could not reach product service");
                    return Mono.error(new ServiceUnavailableException("Product Service Unavailable"));
                })
                .block());
    }

    private Mono<Boolean> webClientCall(Long productId) {
        return webClientBuilder.build().get()
                .uri(API_URL + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.SERVICE_UNAVAILABLE)) {
                        log.error("Could not reach product service");
                        return Mono.error(new ServiceUnavailableException("Product Service Unavailable"));
                    }
                    return Mono.just(response.statusCode().equals(HttpStatus.OK));
                });
    }
}
