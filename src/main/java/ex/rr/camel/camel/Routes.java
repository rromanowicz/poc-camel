package ex.rr.camel.camel;

import com.fasterxml.jackson.core.JsonParseException;
import ex.rr.camel.database.Order;
import ex.rr.camel.database.ProcessingConfirmation;
import ex.rr.camel.service.OrderService;
import ex.rr.camel.service.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class Routes extends RouteBuilder {

    @Value("${server.port}")
    private String port;

    @Override
    public void configure() {

        onException(NoSuchElementException.class)
                .handled(true)
                .setBody(simple("${exchangeProperty[CamelExceptionCaught]}"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("404"));

        onException(JsonParseException.class)
                .handled(true)
                .setBody(simple("${exchangeProperty[CamelExceptionCaught]}"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("400"))
                .stop();


        JacksonDataFormat orderJsonFormat = new JacksonDataFormat(Order.class);
        JacksonDataFormat processingConfirmationJsonFormat = new JacksonDataFormat(ProcessingConfirmation.class);

        restConfiguration()
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Spring Boot Camel Postgres Rest API.")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiContextRouteId("doc-api")
                .port(port)
                .bindingMode(RestBindingMode.json);

        from("spring-rabbitmq:order?queues=q-order.state.pending")
                .routeId("pendingQueueListener")
                .to("direct:pendingOrder");


        from("direct:pendingOrder")
                .routeId("pendingOrder")
                .to("log:input")
                .unmarshal(orderJsonFormat)

                //Split to separate orders
                .split().method(Validator.class, "splitOrder")

                //Select processing route
                .choice()
                    .when(method(Validator.class, "isAdd(${body.items[0].action})"))
                        .to("direct:add")
                    .when(method(Validator.class, "isDelete(${body.items[0].action})"))
                        .to("direct:delete")
                    .when(method(Validator.class, "isNone(${body.items[0].action})"))
                        .to("direct:none")
                .otherwise()
                    .log("Unable to process.")
                .endChoice()
        ;

        from("direct:add")
                .routeId("addRoute")
                .choice()
                    .when(method(Validator.class, "isCoffee(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}] Coffee")
                        .to("direct:addCoffeee")
//                        .transform().method(OrderService.class, "test(${body})")
//                        .log("${body}")
                    .when(method(Validator.class, "isSubscription(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}]: Subscription")
                    .when(method(Validator.class, "isGrinder(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}]: Grinder")
                .endChoice()
                ;

        from("direct:delete")
                .routeId("deleteRoute")
                .choice()
                    .when(method(Validator.class, "isCoffee(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}] Coffee")
                    .when(method(Validator.class, "isSubscription(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}]: Subscription")
                    .when(method(Validator.class, "isGrinder(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}]: Grinder")
                    .endChoice()
                ;

        from("direct:none")
                .routeId("noneRoute")
                .choice()
                    .when(method(Validator.class, "isCoffee(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}] Coffee")
                    .when(method(Validator.class, "isSubscription(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}]: Subscription")
                    .when(method(Validator.class, "isGrinder(${body.items[0].type})"))
                        .log("OrderID: [${body.id}], ItemID: [${body.items[0].id}]: Subscription")
                .endChoice()
                ;

        from("direct:addCoffeee")
                .routeId("addCoffeee")
                .transform().method(OrderService.class, "processOrder(${body})")
                .marshal(processingConfirmationJsonFormat)
//                .to( "spring-rabbitmq:order?routingKey=order.state.processed.routing.key")
                .to(ExchangePattern.InOnly, "spring-rabbitmq:order?routingKey=order.state.processed.routing.key")
                .log("${body}")
        ;


        rest("/order")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)

                .get("/{id}")
                .routeId("getByIdEndpoint")
                .to("{{route.findOrderById}}")

                .get("/")
                .routeId("getAllEndpoint")
                .to("{{route.findAllOrders}}")

                .post("/").type(Order.class)
                .routeId("postEndpoint")
                .to("{{route.saveOrder}}")

                .delete("/{id}")
                .routeId("deleteEndpoint")
                .to("{{route.removeOrder}}");

        from("{{route.findOrderById}}")
                .routeId("findOrderById")
                .log("Received header : ${header.id}")
                .bean(OrderService.class, "findOrderById(${header.id})");

        from("{{route.findAllOrders}}")
                .routeId("findAllOrders")
                .log("findAllOrdersfindAllOrders")
                .bean(OrderService.class, "findAllOrders");

        from("{{route.saveOrder}}")
                .routeId("saveOrder")
                .log("Received Body ${body}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("201"))
                .transform().method(OrderService.class, "saveOrder(${body})")
                .marshal(orderJsonFormat)
                .to("direct:pendingOrder");

        from("{{route.removeOrder}}")
                .routeId("removeOrder")
                .log("Received header : ${header.id}")
                .bean(OrderService.class, "removeOrder(${header.id})");

    }
}

