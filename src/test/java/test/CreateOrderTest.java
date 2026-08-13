package test;

import client.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import model.Order;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.OrderGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private OrderClient orderClient;
    private final List<String> colors;

    public CreateOrderTest(List<String> colors) {
        this.colors = colors;
    }

    @Parameterized.Parameters(name = "Цвета: {0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList("BLACK")},
                {Arrays.asList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {null},
                {Arrays.asList()}
        });
    }

    @Before
    @Step("Инициализация клиента заказов")
    public void setUp() {
        orderClient = new OrderClient();
    }

    private boolean isServerAvailable() {
        try {
            var response = orderClient.getOrders();
            return response.statusCode() == HttpStatus.SC_OK;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Создание заказа с различными вариантами цвета")
    @Description("Параметризованный тест проверяет создание заказа с разными комбинациями цветов")
    public void testCreateOrderWithDifferentColors() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        Order order = OrderGenerator.getOrderWithColors(colors);
        var response = orderClient.createOrder(order);

        assertEquals("Статус код должен быть 201", HttpStatus.SC_CREATED, response.statusCode());
        assertTrue("track должен быть положительным",
                response.jsonPath().getInt("track") > 0);
    }
}