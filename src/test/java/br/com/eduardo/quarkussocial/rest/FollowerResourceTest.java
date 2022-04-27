package br.com.eduardo.quarkussocial.rest;

import br.com.eduardo.quarkussocial.domain.model.Follower;
import br.com.eduardo.quarkussocial.domain.model.User;
import br.com.eduardo.quarkussocial.domain.repository.FollowerRepository;
import br.com.eduardo.quarkussocial.domain.repository.UserRepository;
import br.com.eduardo.quarkussocial.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp(){
        // usuario padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        // usuário seguidor
        var follower = new User();
        follower.setAge(30);
        follower.setName("Ciclano");
        userRepository.persist(follower);
        followerId = follower.getId();

        // criar um seguidor para o usuário
        var followerEntity = new Follower();
        followerEntity.setUser(user);
        followerEntity.setFollower(follower);

        followerRepository.persist(followerEntity);

    }

    @Test
    @DisplayName("Should return 409 when Follower id is equal to User id")
    public void saveUserAsFollowerTest(){

        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .pathParam("userId", userId)
        .when()
            .put()
        .then()
            .statusCode(Response.Status.CONFLICT.getStatusCode())
            .body(Matchers.is("You can't follow yourself"))
        ;

    }

    @Test
    @DisplayName("Should return 404 on follow a user when User id doesn't exists")
    public void userNotFoundWhenTryingFollowTest(){

        var inexistentUserId = 999;

        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .pathParam("userId", inexistentUserId)
        .when()
            .put()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode())
        ;

    }

    @Test
    @DisplayName("Should follow a User")
    public void followUserTest(){

        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .pathParam("userId", userId)
        .when()
            .put()
        .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode())
        ;

    }

    @Test
    @DisplayName("Should return 404 on list a user followers and User id doesn't exists")
    public void userNotFoundWhenTryingListingFollowersTest(){

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .pathParam("userId", inexistentUserId)
        .when()
            .get()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode())
        ;

    }

    @Test
    @DisplayName("Should list a user's followers")
    public void listFollowersTest(){

        var response = given()
            .contentType(ContentType.JSON)
            .pathParam("userId", userId)
        .when()
            .get()
        .then()
            .extract()
            .response()
        ;


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        var followersCount = response.jsonPath().get("followersCount");
        assertEquals(1, followersCount);
        var followersContent = response.jsonPath().getList("content");
        assertEquals(1, followersContent.size());

    }

    @Test
    @DisplayName("Should return 404 on unfollow user and User id doesn't exists")
    public void userNotFoundWhenUnfollowingAUserTest(){

        var inexistentUserId = 999;

        given()
            .pathParam("userId", inexistentUserId)
            .queryParam("followerId", followerId)
        .when()
            .delete()
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode())
        ;

    }

    @Test
    @DisplayName("Should Unfollow an user")
    public void unfollowUserTest(){

        given()
            .pathParam("userId", userId)
            .queryParam("followerId", followerId)
        .when()
            .delete()
        .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode())
        ;

    }

}