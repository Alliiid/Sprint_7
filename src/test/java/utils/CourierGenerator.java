package utils;

import io.qameta.allure.Step;
import model.Courier;

import java.util.UUID;

public class CourierGenerator {

    @Step("Генерация случайного курьера")
    public static Courier getRandomCourier() {
        String uniqueLogin = "test_courier_" + UUID.randomUUID().toString().substring(0, 8);
        return new Courier(uniqueLogin, "TestPass123!", "ТестовыйКурьер");
    }

    @Step("Генерация курьера с отсутствующим полем: {missingField}")
    public static Courier getCourierWithMissingField(String missingField) {
        Courier courier = getRandomCourier();
        switch (missingField) {
            case "login":
                courier.setLogin(null);
                break;
            case "password":
                courier.setPassword(null);
                break;
            case "firstName":
                courier.setFirstName(null);
                break;
            default:
                throw new IllegalArgumentException("Неизвестное поле: " + missingField);
        }
        return courier;
    }

    @Step("Создание курьера с указанным логином: {login}")
    public static Courier getCourierWithLogin(String login) {
        return new Courier(login, "TestPass123!", "ТестовыйКурьер");
    }
}