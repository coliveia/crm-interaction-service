package com.vivo.crm.interaction.application.service;

import com.vivo.crm.shared.dto.tmf629.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * InteractionService Reactive - Integrado com TMF629 Customer Management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceReactive {

    private final WebClient webClient;

    /**
     * Busca dados do Customer via API REST TMF629
     */
    public Mono<CustomerDTO> getCustomerById(String customerId) {
        return webClient
            .get()
            .uri("/tmf-api/customerManagement/v4/customer/{id}", customerId)
            .retrieve()
            .bodyToMono(CustomerDTO.class)
            .doOnError(error -> log.error("Erro ao buscar customer: {}", customerId, error));
    }

    /**
     * Valida se o customer existe
     */
    public Mono<Boolean> validateCustomer(String customerId) {
        return getCustomerById(customerId)
            .map(customer -> customer.getId() != null)
            .onErrorReturn(false);
    }

    /**
     * Busca customer por phone
     */
    public Mono<CustomerDTO> getCustomerByPhone(String phone) {
        return webClient
            .get()
            .uri("/tmf-api/customerManagement/v4/customer?phone={phone}", phone)
            .retrieve()
            .bodyToMono(CustomerDTO.class)
            .doOnError(error -> log.error("Erro ao buscar customer por phone: {}", phone, error));
    }

    /**
     * Busca customer por email
     */
    public Mono<CustomerDTO> getCustomerByEmail(String email) {
        return webClient
            .get()
            .uri("/tmf-api/customerManagement/v4/customer?email={email}", email)
            .retrieve()
            .bodyToMono(CustomerDTO.class)
            .doOnError(error -> log.error("Erro ao buscar customer por email: {}", email, error));
    }
}
