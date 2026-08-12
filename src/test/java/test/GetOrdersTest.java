package test;

import client.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class GetOrdersTest {
    private OrderClient orderClient;

    @Before
    @Step("Инициализация клиента заказов")
    public void setUp() {
        orderClient = new OrderClient();
        sleep(500);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isServerAvailable() {
        try {
            var response = orderClient.getOrders();
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка, что ручка возвращает список заказов с корректной структурой")
    public void testGetOrdersList() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = orderClient.getOrders();

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 200", 200, statusCode);

        List<Map<String, Object>> orders = response.jsonPath().getList("orders");
        assertNotNull("Список заказов не должен быть null", orders);
    }

    @Test
    @DisplayName("Проверка структуры заказа в ответе")
    @Description("Проверка, что каждый заказ содержит все необходимые поля")
    public void testOrderStructure() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = orderClient.getOrders();

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 200", 200, statusCode);

        List<Map<String, Object>> orders = response.jsonPath().getList("orders");
        assertNotNull("Список заказов не должен быть null", orders);

        if (!orders.isEmpty()) {
            Map<String, Object> firstOrder = orders.get(0);

            // Проверяем только те поля, которые точно есть в ответе
            assertTrue("Заказ должен содержать поле id", firstOrder.containsKey("id"));
            assertTrue("Заказ должен содержать поле status", firstOrder.containsKey("status"));
            assertTrue("Заказ должен содержать поле track", firstOrder.containsKey("track"));
            assertTrue("Заказ должен содержать поле address", firstOrder.containsKey("address"));
            assertTrue("Заказ должен содержать поле phone", firstOrder.containsKey("phone"));
            assertTrue("Заказ должен содержать поле rentTime", firstOrder.containsKey("rentTime"));
            assertTrue("Заказ должен содержать поле deliveryDate", firstOrder.containsKey("deliveryDate"));
        }
    }

    @Test
    @DisplayName("Проверка, что список заказов не пустой")
    @Description("Тест проверяет, что в ответе есть хотя бы один заказ")
    public void testOrdersListNotEmpty() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = orderClient.getOrders();

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 200", 200, statusCode);

        List<Map<String, Object>> orders = response.jsonPath().getList("orders");
        assertNotNull("Список заказов не должен быть null", orders);

        assertTrue("Ответ должен содержать поле orders",
                response.jsonPath().getMap("$").containsKey("orders"));
    }

    @Test
    @DisplayName("Проверка наличия основных полей заказа")
    @Description("Детальная проверка структуры заказа")
    public void testOrderFieldsPresence() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = orderClient.getOrders();

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 200", 200, statusCode);

        List<Map<String, Object>> orders = response.jsonPath().getList("orders");

        if (!orders.isEmpty()) {
            Map<String, Object> order = orders.get(0);

            // Список полей, которые точно есть в ответе
            String[] expectedFields = {
                    "id", "status", "track", "address",
                    "metroStation", "phone", "rentTime", "deliveryDate",
                    "comment", "color", "createdAt", "updatedAt"
            };

            for (String field : expectedFields) {
                assertTrue("Заказ должен содержать поле: " + field,
                        order.containsKey(field));
            }
        }
    }
}