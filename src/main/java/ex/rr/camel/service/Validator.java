package ex.rr.camel.service;

import ex.rr.camel.database.Action;
import ex.rr.camel.database.Item;
import ex.rr.camel.database.ItemType;
import ex.rr.camel.database.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class Validator {

    public boolean isCoffee(ItemType type) {
        return type == ItemType.COFFEE;
    }

    public boolean isSubscription(ItemType type) {
        return type == ItemType.SUBSCRIPTION;
    }

    public boolean isGrinder(ItemType type) {
        return type == ItemType.GRINDER;
    }

    public boolean isAdd(Action action) {
        return action == Action.ADD;
    }

    public boolean isDelete(Action action) {
        return action == Action.DELETE;
    }

    public boolean isNone(Action action) {
        return action == Action.NONE;
    }

    public List<Order> splitOrder(Order order) {
        List<Order> items = new ArrayList<>();
        order.getItems().forEach(item -> {
            items.add(newOrder(order, item));
        });
        return items;
    }

    private Order newOrder(Order order, Item item) {
        return new Order(
                order.getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCompletedAt(),
                order.getUsers(),
                order.getPayment(),
                Collections.singletonList(item));
    }

}