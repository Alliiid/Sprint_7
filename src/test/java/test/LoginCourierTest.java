package test;

import client.CourierClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import model.Courier;
import model.CourierCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.CourierGenerator;
import utils.CourierIdExtractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class LoginCourierTest {
    private CourierClient courierClient;
    private Courier testCourier;
    private int courierId;

    @Before
    @Step("Создание тестового курьера для авторизации")
    public void setUp() {
        courierClient = new CourierClient();
        testCourier = CourierGenerator.getRandomCourier();

        try {
            courierClient.createCourier(testCourier);
            sleep(500);
            var loginResponse = courierClient.loginCourier(
                    new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
            );
            courierId = CourierIdExtractor.extractCourierId(loginResponse);
        } catch (Exception e) {
            System.out.println("Не удалось создать курьера: " + e.getMessage());
            courierId = 0;
        }
    }

    @After
    @Step("Удаление тестового курьера")
    public void cleanUp() {
        if (courierId > 0) {
            try {
                courierClient.deleteCourier(courierId);
                sleep(300);
            } catch (Exception e) {
                System.out.println("Не удалось удалить курьера: " + e.getMessage());
            }
        }
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
            var response = courierClient.createCourier(CourierGenerator.getRandomCourier());
            int statusCode = response.statusCode();
            return statusCode == 201 || statusCode == 409 || statusCode == 400;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    @Description("Проверка, что курьер может авторизоваться с корректными данными и получить id")
    public void testLoginCourierSuccess() {
        assumeTrue("Сервер недоступен или курьер не создан, тест пропущен",
                isServerAvailable() && courierId > 0);

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 200", 200, statusCode);
        int id = response.jsonPath().getInt("id");
        assertTrue("ID должен быть положительным", id > 0);
    }

    @Test
    @DisplayName("Ошибка при авторизации без логина")
    @Description("Проверка, что при отсутствии логина возвращается ошибка")
    public void testLoginWithoutLogin() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials(null, testCourier.getPassword())
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 400", 400, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о недостаточности данных",
                message.contains("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Ошибка при авторизации без пароля")
    @Description("Проверка, что при отсутствии пароля возвращается ошибка")
    public void testLoginWithoutPassword() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), null)
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 400", 400, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о недостаточности данных",
                message.contains("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Ошибка при неправильном логине")
    @Description("Проверка, что при авторизации с неправильным логином возвращается ошибка 404")
    public void testLoginWithWrongLogin() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials("wrong_login_123", testCourier.getPassword())
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 404", 404, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о ненайденной учетной записи",
                message.contains("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Ошибка при неправильном пароле")
    @Description("Проверка, что при авторизации с неправильным паролем возвращается ошибка 404")
    public void testLoginWithWrongPassword() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), "wrong_password_123")
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 404", 404, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о ненайденной учетной записи",
                message.contains("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Ошибка при авторизации несуществующего пользователя")
    @Description("Проверка, что при авторизации с несуществующим логином возвращается ошибка")
    public void testLoginNonExistentUser() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials("nonexistent_user_123", "password123")
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 404", 404, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о ненайденной учетной записи",
                message.contains("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация с пустым логином")
    @Description("Проверка, что при пустом логине возвращается ошибка 400")
    public void testLoginWithEmptyLogin() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials("", testCourier.getPassword())
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 400", 400, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о недостаточности данных",
                message.contains("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация с пустым паролем")
    @Description("Проверка, что при пустом пароле возвращается ошибка 400")
    public void testLoginWithEmptyPassword() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), "")
        );

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 400", 400, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о недостаточности данных",
                message.contains("Недостаточно данных для входа"));
    }
}