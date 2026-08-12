package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Order;

import static io.restassured.RestAssured.given;

public class OrderClient extends RestClient {
    private static final String ORDER_PATH = "/api/v1/orders";

    @Step("Создание заказа для {order.firstName} {order.lastName}")
    public Response createOrder(Order order) {
        return given()
                .spec(getBaseSpec())
                .body(order)
                .when()
                .post(ORDER_PATH);
    }

    @Step("Получение списка всех заказов")
    public Response getOrders() {
        return given()
                .spec(getBaseSpec())
                .when()
                .get(ORDER_PATH);
    }
}
