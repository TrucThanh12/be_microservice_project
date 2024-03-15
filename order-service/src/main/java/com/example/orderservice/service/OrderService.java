package com.example.orderservice.service;

import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderLineItemsDto;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderlineItems;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;
    
    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderlineItems> orderlineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderlineItemsList(orderlineItemsList);
        
        List<String> skuCodes = order.getOrderlineItemsList().stream()
                .map(OrderlineItems::getSkuCode)
                .toList();
        // call inventory service, and place order if product is in
        // stock
        InventoryResponse[] inventoryResponsesArray = webClient.get()
                                    .uri("http://localhost:8082/api/inventory",
                                            uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                                    .retrieve()
                                    .bodyToMono(InventoryResponse[].class)
                                    .block();
        if(Objects.isNull(inventoryResponsesArray)){
            log.info("(placeOrder) inventoryResponsesArray is null");
        }
        else {
            boolean allProductsInStock = Arrays.stream(inventoryResponsesArray)
                    .allMatch(InventoryResponse::isInStock);
            if(allProductsInStock){
                orderRepository.save(order);
            }
            else{
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
        }
    }

    private OrderlineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderlineItems orderlineItems = new OrderlineItems();
        orderlineItems.setPrice(orderLineItemsDto.getPrice());
        orderlineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderlineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderlineItems;
    }
}
