package com.example.orderservice.service;

import com.example.orderservice.dto.OrderLineItemsDto;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderlineItems;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    
    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderlineItems> orderlineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderlineItemsList(orderlineItemsList);
        orderRepository.save(order);
    }

    private OrderlineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderlineItems orderlineItems = new OrderlineItems();
        orderlineItems.setPrice(orderLineItemsDto.getPrice());
        orderlineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderlineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderlineItems;
    }
}
