package br.com.eduardo.quarkussocial.rest;

import br.com.eduardo.quarkussocial.domain.model.Follower;
import br.com.eduardo.quarkussocial.domain.model.Post;
import br.com.eduardo.quarkussocial.domain.model.User;
import br.com.eduardo.quarkussocial.domain.repository.FollowerRepository;
import br.com.eduardo.quarkussocial.domain.repository.PostRepository;
import br.com.eduardo.quarkussocial.domain.repository.UserRepository;
import br.com.eduardo.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)// outra forma de dizer qual a url principal
class PostResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        // usuario padrão dos testes
        var user = new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        // cria uma postagem para o usuário
        var post = new Post();
        post.setPostText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        //usuario que não segue ninguém
        var userNotFollower = new User();
        userNotFollower.setAge(40);
        userNotFollower.setName("Ciclano");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //usuario seguidor
        var userFollower = new User();
        userFollower.setAge(40);
        userFollower.setName("Follower");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        var follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);

        followerRepository.persist(follower);

    }

    @Test
    @DisplayName("Should create a post for a user")
    public void createPostTest(){

        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        given()
            .contentType(ContentType.JSON)
            .body(postRequest)
            .pathParam("userId", userId)
        .when()
            .post()
        .then()
        .statusCode(201);


    }

    @Test
    @DisplayName("Should return 404 when trying to make a post for an inexistent user")
    public void postForAnInexsistentUserTest(){

        var postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        var inexistentUserId = 999;

        given()
            .contentType(ContentType.JSON)
            .body(postRequest)
            .pathParam("userId", inexistentUserId)
        .when()
            .post()
        .then()
            .statusCode(404);

    }

    @Test
    @DisplayName("Should return 404 when user doesn't exists")
    public void listPostNotFoundTest(){

        var inexistentUserId = 999;

        given()
            .pathParam("userId", inexistentUserId)
        .when()
            .get()
        .then()
            .statusCode(404);


    }

    @Test
    @DisplayName("Should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest(){

        given()
            .pathParam("userId", userId)
        .when()
            .get()
        .then()
            .statusCode(400)
            .body(Matchers.is("You forgot this header followerId"));
    }

    @Test
    @DisplayName("Should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest(){

        var inexistentFollowerId = 999;

        given()
            .pathParam("userId", userId)
            .header("followerId", inexistentFollowerId)
        .when()
            .get()
        .then()
            .statusCode(400)
            .body(Matchers.is("Nonexistent follower user"));
    }

    @Test
    @DisplayName("Should return 403 when follower isn't a follower")
    public void listPostNotAFollower(){

        given()
            .pathParam("userId", userId)
            .header("followerId", userNotFollowerId)
        .when()
            .get()
        .then()
            .statusCode(403)
            .body(Matchers.is("You can't see these posts"));

    }

    @Test
    @DisplayName("Should return posts")
    public void listPostsTest(){
        given()
            .pathParam("userId", userId)
            .header("followerId", userFollowerId)
        .when()
            .get()
            .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

}