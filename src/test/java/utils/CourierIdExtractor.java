package utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;

public class CourierIdExtractor {

    @Step("Извлечение id курьера из ответа")
    public static int extractCourierId(Response response) {
        return response.jsonPath().getInt("id");
    }
}
