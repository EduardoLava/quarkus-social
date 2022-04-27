package br.com.eduardo.quarkussocial.rest;

import br.com.eduardo.quarkussocial.rest.dto.CreateUserRequest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiUrl;

    @Test
    @DisplayName("Should create an user successfully")
    @Order(1)
    public void createUserTest(){

        // cenario
        var user = new CreateUserRequest();
        user.setName("Fulano");
        user.setAge(30);

        var response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(user)
        .when()// execucao
            .post(apiUrl)
        .then()// resposta
            .extract()
            .response();

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.jsonPath().getString("id"));// que tenha um id

    }

    @Test
    @DisplayName("Should return error when json is not valid")
    @Order(2)
    public void createUserValidationErrorTest(){

        // cenario
        var user = new CreateUserRequest();
        user.setName(null);
        user.setAge(null);

        var response = RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(user)
            .when()
                .post(apiUrl)
            .then()
                .extract()
                .response()
                ;

        assertEquals(400, response.getStatusCode());
        assertEquals("Validation error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");

        assertNotNull(errors.get(0).get("message"));
//        assertEquals("Age is required", errors.get(0).get("message"));
        assertNotNull(errors.get(1).get("message"));
//        assertEquals("Name is required", errors.get(1).get("message"));
    }

    @Test
    @DisplayName("Should list all users")
    @Order(3)
    public void listAllUsersTest(){

        RestAssured.given()
            .contentType(ContentType.JSON)
        .when()
            .get(apiUrl)
        .then()
            .statusCode(200)
            .body("size()", Matchers.is(1));

    }

}