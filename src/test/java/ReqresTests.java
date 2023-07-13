import models.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.qameta.allure.Allure.step;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.ReqresSpecs.*;

public class ReqresTests {
    @Test
    @DisplayName("Получение данных о пользователе")
    void getSingleUserTest () {
        step("Make GET request by Id and check response body for conformance to scheme",() ->
        given (requestSpec)
                .when()
                .get("/users/2")
                .then()
                .spec(responseCode200Spec)
                .body(matchesJsonSchemaInClasspath("schemes/status-response-scheme.json")));

    }
    @Test
    @DisplayName("Получение данных о несуществующем пользователе")
    void resourceNotFoundTest () {
        step("Make GET request with wrong Id and check error in response", () ->
        given(requestSpec)
                .when()
                .get("/unknown/23")
                .then()
                .spec(responseCode404spec));
    }

    @Test
    @DisplayName("Создание пользователя")
    void successfulRegisterTest () {
        int userId = 4;
        String token = "QpwL5tke4Pnpja7X4";

        RegisterUserBodyModel requestBody = new RegisterUserBodyModel();
        requestBody.setEmail("eve.holt@reqres.in");
        requestBody.setPassword("pistol");
        RegisterUserResponseModel registerResponse = step ("Make request with user data", () ->
        given(requestSpec)
                .body(requestBody)
                .when()
                .post("/register")
                .then()
                .spec(responseCode200Spec)
                .extract().as(RegisterUserResponseModel.class));
        step ("Check data in response", () -> {
            assertEquals(userId, registerResponse.getId());
            assertEquals(token, registerResponse.getToken());
        });
        }
    @Test
    @DisplayName("Неуспешная авторизация без пароля")
    void unsuccessfulLoginTest () {
        RegisterUserBodyModel requestBody = new RegisterUserBodyModel();
        requestBody.setEmail("peter@klaven");
        step("Make request and check response", () ->
        given(requestSpec)
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .spec(responseCode400Spec)
                .body("error", is("Missing password")));
    }
    @Test
    @DisplayName("Редактирование данных пользователя")
    void userUpdateTest () {
       String userName = "morpheus";
       String userJob = "zion resident";

        UpdateUserBodyModel requestBody = new UpdateUserBodyModel();
        requestBody.setName(userName);
        requestBody.setJob(userJob);
        UpdateUserResponseModel updateUserResponse = step ("Make request with user data", () ->
        given(requestSpec)
                .body(requestBody)
                .when()
                .put("/users/2")
                .then()
                .spec(responseCode200Spec)
                .body("name", is("morpheus"))
                .body("job", is("zion resident"))
                .extract().as(UpdateUserResponseModel.class));
        step ("Check data in response", () -> {
            assertEquals(userName, updateUserResponse.getName());
            assertEquals(userJob, updateUserResponse.getJob());
        });
    }

    @Test
    @DisplayName("Получение списка пользователей")
    void getUsersListTest () {
        int pageId = 2;
        List<Integer> expectedIds = List.of(7, 8, 9, 10, 11, 12);

        UsersResponseModel usersResponse = step ("Make request with pageId", () ->
            given(requestSpec)
                    .when()
                    .get("/users?page=" + pageId)
                    .then()
                    .spec(responseCode200Spec)
                    .extract().as(UsersResponseModel.class));

        step ("Check data in response", () -> {
            assertEquals(pageId, usersResponse.getPage());

            ArrayList<Integer> responseIds = new ArrayList<>();
            usersResponse.getUsers().forEach((k) -> responseIds.add(k.getId()));
            assertEquals(expectedIds, responseIds);
        });
    }
}
