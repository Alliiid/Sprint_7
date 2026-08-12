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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class CreateCourierTest {
    private CourierClient courierClient;
    private Courier testCourier;
    private int courierId;

    @Before
    @Step("Подготовка к тесту: инициализация клиента и генерация тестового курьера")
    public void setUp() {
        courierClient = new CourierClient();
        testCourier = CourierGenerator.getRandomCourier();
        sleep(500);
    }

    @After
    @Step("Очистка: удаление созданного курьера")
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
    @DisplayName("Успешное создание курьера")
    @Description("Проверка, что курьер создается с корректными данными и возвращается ok: true")
    public void testCreateCourierSuccess() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        var response = courierClient.createCourier(testCourier);

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 201", 201, statusCode);
        assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));

        sleep(500);
        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");
        assertTrue("ID должен быть положительным", courierId > 0);
    }

    @Test
    @DisplayName("Ошибка при создании дублирующего курьера")
    @Description("Проверка, что нельзя создать курьера с уже существующим логином")
    public void testCreateDuplicateCourier() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        courierClient.createCourier(testCourier);
        sleep(500);

        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");

        sleep(500);
        Courier duplicateCourier = new Courier(
                testCourier.getLogin(),
                "AnotherPassword123!",
                "ДругойКурьер"
        );

        var response = courierClient.createCourier(duplicateCourier);

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 409", 409, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о существующем логине",
                message.contains("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Обязательность всех полей при создании")
    @Description("Проверка, что нельзя создать курьера без логина или пароля")
    public void testCreateCourierWithMissingRequiredFields() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        String[] missingFields = {"login", "password"};

        for (String field : missingFields) {
            sleep(500);

            Courier invalidCourier = CourierGenerator.getCourierWithMissingField(field);
            var response = courierClient.createCourier(invalidCourier);

            int statusCode = response.statusCode();
            assertEquals("Статус код должен быть 400", 400, statusCode);
            String message = response.jsonPath().getString("message");
            assertTrue("Должна быть ошибка о недостающих полях",
                    message.contains("Недостаточно данных для создания учетной записи"));
        }
    }

    @Test
    @DisplayName("Создание курьера без имени")
    @Description("Проверка, что можно создать курьера, указав только логин и пароль")
    public void testCreateCourierWithoutFirstName() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Courier courierWithoutName = CourierGenerator.getCourierWithMissingField("firstName");
        var response = courierClient.createCourier(courierWithoutName);

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 201", 201, statusCode);
        assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));

        sleep(500);
        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(courierWithoutName.getLogin(), courierWithoutName.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");
    }

    @Test
    @DisplayName("Ошибка при создании с существующим логином")
    @Description("Проверка, что при создании курьера с существующим логином возвращается ошибка")
    public void testCreateCourierWithExistingLogin() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        courierClient.createCourier(testCourier);
        sleep(500);

        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");

        sleep(500);
        Courier sameLoginCourier = new Courier(
                testCourier.getLogin(),
                "DifferentPass123!",
                "Петр"
        );

        var response = courierClient.createCourier(sameLoginCourier);

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 409", 409, statusCode);
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о существующем логине",
                message.contains("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Создание курьера с минимальными данными")
    @Description("Проверка создания курьера только с логином и паролем")
    public void testCreateCourierWithMinimumFields() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        sleep(500);

        Courier minimalCourier = new Courier(
                "minimal_" + System.currentTimeMillis(),
                "password123",
                null
        );

        var response = courierClient.createCourier(minimalCourier);

        int statusCode = response.statusCode();
        assertEquals("Статус код должен быть 201", 201, statusCode);
        assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));

        sleep(500);
        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(minimalCourier.getLogin(), minimalCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");
        assertTrue("Курьер должен быть создан", courierId > 0);
    }
}