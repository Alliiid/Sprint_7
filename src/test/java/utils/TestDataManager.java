package utils;

import client.CourierClient;
import model.Courier;
import model.CourierCredentials;
import io.qameta.allure.Step;

public class TestDataManager {
    private static final CourierClient courierClient = new CourierClient();

    @Step("Создание и авторизация тестового курьера")
    public static Courier createAndLoginTestCourier() {
        Courier courier = CourierGenerator.getRandomCourier();
        courierClient.createCourier(courier);
        return courier;
    }

    @Step("Получение ID курьера по логину и паролю")
    public static int getCourierId(Courier courier) {
        var response = courierClient.loginCourier(
                new CourierCredentials(courier.getLogin(), courier.getPassword())
        );
        return CourierIdExtractor.extractCourierId(response);
    }

    @Step("Удаление курьера по ID: {courierId}")
    public static void deleteCourier(int courierId) {
        if (courierId > 0) {
            courierClient.deleteCourier(courierId);
        }
    }
}
