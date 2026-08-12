package test;

import client.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import model.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.OrderGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private OrderClient orderClient;
    private final List<String> colors;
    private final String testDescription;

    public CreateOrderTest(List<String> colors, String testDescription) {
        this.colors = colors;
        this.testDescription = testDescription;
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList("BLACK"), "Только BLACK цвет"},
                {Arrays.asList("GREY"), "Только GREY цвет"},
                {Arrays.asList("BLACK", "GREY"), "Оба цвета: BLACK и GREY"},
                {null, "Без указания цвета"},
                {Arrays.asList(), "Пустой список цветов"}
        });
    }

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
    @DisplayName("Создание заказа с различными вариантами цвета")
    @Description("Параметризованный тест проверяет создание заказа с разными комбинациями цветов")
    public void testCreateOrderWithDifferentColors() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Order order = OrderGenerator.getOrderWithColors(colors);
        var response = orderClient.createOrder(order);

        int statusCode = response.statusCode();
        assertTrue("Статус код должен быть 201, получен: " + statusCode, statusCode == 201);
        int track = response.jsonPath().getInt("track");
        assertTrue("track должен быть положительным", track > 0);
    }

    @Test
    @DisplayName("Создание заказа только с BLACK цветом")
    @Description("Проверка создания заказа с указанием только черного цвета")
    public void testCreateOrderWithBlackOnly() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Order order = OrderGenerator.getOrderWithColors("BLACK");
        var response = orderClient.createOrder(order);

        int statusCode = response.statusCode();
        assertTrue("Статус код должен быть 201, получен: " + statusCode, statusCode == 201);
        int track = response.jsonPath().getInt("track");
        assertTrue("track должен быть положительным", track > 0);
    }

    @Test
    @DisplayName("Создание заказа только с GREY цветом")
    @Description("Проверка создания заказа с указанием только серого цвета")
    public void testCreateOrderWithGreyOnly() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Order order = OrderGenerator.getOrderWithColors("GREY");
        var response = orderClient.createOrder(order);

        int statusCode = response.statusCode();
        assertTrue("Статус код должен быть 201, получен: " + statusCode, statusCode == 201);
        int track = response.jsonPath().getInt("track");
        assertTrue("track должен быть положительным", track > 0);
    }

    @Test
    @DisplayName("Создание заказа с обоими цветами")
    @Description("Проверка создания заказа с указанием обоих цветов: BLACK и GREY")
    public void testCreateOrderWithBothColors() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Order order = OrderGenerator.getOrderWithColors("BLACK", "GREY");
        var response = orderClient.createOrder(order);

        int statusCode = response.statusCode();
        assertTrue("Статус код должен быть 201, получен: " + statusCode, statusCode == 201);
        int track = response.jsonPath().getInt("track");
        assertTrue("track должен быть положительным", track > 0);
    }

    @Test
    @DisplayName("Создание заказа без указания цвета")
    @Description("Проверка создания заказа без указания цвета - это допустимо")
    public void testCreateOrderWithoutColor() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Order order = OrderGenerator.getDefaultOrder();
        var response = orderClient.createOrder(order);

        int statusCode = response.statusCode();
        assertTrue("Статус код должен быть 201, получен: " + statusCode, statusCode == 201);
        int track = response.jsonPath().getInt("track");
        assertTrue("track должен быть положительным", track > 0);
    }

    @Test
    @DisplayName("Создание заказа с пустым списком цветов")
    @Description("Проверка создания заказа с пустым массивом цветов")
    public void testCreateOrderWithEmptyColorArray() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Order order = OrderGenerator.getOrderWithColors(Arrays.asList());
        var response = orderClient.createOrder(order);

        int statusCode = response.statusCode();
        assertTrue("Статус код должен быть 201, получен: " + statusCode, statusCode == 201);
        int track = response.jsonPath().getInt("track");
        assertTrue("track должен быть положительным", track > 0);
    }
}