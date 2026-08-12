package utils;

import io.qameta.allure.Step;
import model.Order;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class OrderGenerator {
    private static final String DEFAULT_FIRST_NAME = "Иван";
    private static final String DEFAULT_LAST_NAME = "Петров";
    private static final String DEFAULT_ADDRESS = "ул. Ленина, д. 1";
    private static final String DEFAULT_METRO_STATION = "4";
    private static final String DEFAULT_PHONE = "+79991234567";
    private static final int DEFAULT_RENT_TIME = 3;
    private static final String DEFAULT_COMMENT = "Позвонить за час до приезда";

    @Step("Генерация стандартного заказа")
    public static Order getDefaultOrder() {
        String deliveryDate = LocalDate.now().plusDays(3)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return new Order(
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_ADDRESS,
                DEFAULT_METRO_STATION,
                DEFAULT_PHONE,
                DEFAULT_RENT_TIME,
                deliveryDate,
                DEFAULT_COMMENT,
                null
        );
    }

    @Step("Генерация заказа с цветами: {colors}")
    public static Order getOrderWithColors(List<String> colors) {
        Order order = getDefaultOrder();
        order.setColor(colors);
        return order;
    }

    @Step("Генерация заказа с цветами: {colors}")
    public static Order getOrderWithColors(String... colors) {
        return getOrderWithColors(Arrays.asList(colors));
    }
}