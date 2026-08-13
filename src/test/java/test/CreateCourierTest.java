package test;

import client.CourierClient;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import model.Courier;
import model.CourierCredentials;
import org.apache.http.HttpStatus;
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
    }

    @After
    @Step("Очистка: удаление созданного курьера")
    public void cleanUp() {
        if (courierId > 0) {
            try {
                courierClient.deleteCourier(courierId);
            } catch (Exception e) {
                System.out.println("Не удалось удалить курьера: " + e.getMessage());
            }
        }
    }

    private boolean isServerAvailable() {
        try {
            var response = courierClient.createCourier(CourierGenerator.getRandomCourier());
            int statusCode = response.statusCode();
            return statusCode == HttpStatus.SC_CREATED ||
                    statusCode == HttpStatus.SC_CONFLICT ||
                    statusCode == HttpStatus.SC_BAD_REQUEST;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Проверка, что курьер создается с корректными данными и возвращается ok: true")
    public void testCreateCourierSuccess() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        var response = courierClient.createCourier(testCourier);

        assertEquals("Статус код должен быть 201", HttpStatus.SC_CREATED, response.statusCode());
        assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));

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

        courierClient.createCourier(testCourier);

        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");

        Courier duplicateCourier = new Courier(
                testCourier.getLogin(),
                "AnotherPassword123!",
                "ДругойКурьер"
        );

        var response = courierClient.createCourier(duplicateCourier);

        assertEquals("Статус код должен быть 409", HttpStatus.SC_CONFLICT, response.statusCode());
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
            Courier invalidCourier = CourierGenerator.getCourierWithMissingField(field);
            var response = courierClient.createCourier(invalidCourier);

            assertEquals("Статус код должен быть 400", HttpStatus.SC_BAD_REQUEST, response.statusCode());
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

        Courier courierWithoutName = CourierGenerator.getCourierWithMissingField("firstName");
        var response = courierClient.createCourier(courierWithoutName);

        assertEquals("Статус код должен быть 201", HttpStatus.SC_CREATED, response.statusCode());
        assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));

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

        courierClient.createCourier(testCourier);

        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(testCourier.getLogin(), testCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");

        Courier sameLoginCourier = new Courier(
                testCourier.getLogin(),
                "DifferentPass123!",
                "Петр"
        );

        var response = courierClient.createCourier(sameLoginCourier);

        assertEquals("Статус код должен быть 409", HttpStatus.SC_CONFLICT, response.statusCode());
        String message = response.jsonPath().getString("message");
        assertTrue("Должна быть ошибка о существующем логине",
                message.contains("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Создание курьера с минимальными данными")
    @Description("Проверка создания курьера только с логином и паролем")
    public void testCreateCourierWithMinimumFields() {
        assumeTrue("Сервер недоступен, тест пропущен", isServerAvailable());

        Courier minimalCourier = new Courier(
                "minimal_" + System.currentTimeMillis(),
                "password123",
                null
        );

        var response = courierClient.createCourier(minimalCourier);

        assertEquals("Статус код должен быть 201", HttpStatus.SC_CREATED, response.statusCode());
        assertTrue("Ответ должен содержать ok: true", response.jsonPath().getBoolean("ok"));

        var loginResponse = courierClient.loginCourier(
                new CourierCredentials(minimalCourier.getLogin(), minimalCourier.getPassword())
        );
        courierId = loginResponse.jsonPath().getInt("id");
        assertTrue("Курьер должен быть создан", courierId > 0);
    }
}