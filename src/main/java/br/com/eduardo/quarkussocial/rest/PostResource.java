package br.com.eduardo.quarkussocial.rest;

import br.com.eduardo.quarkussocial.domain.model.Post;
import br.com.eduardo.quarkussocial.domain.model.User;
import br.com.eduardo.quarkussocial.domain.repository.FollowerRepository;
import br.com.eduardo.quarkussocial.domain.repository.PostRepository;
import br.com.eduardo.quarkussocial.domain.repository.UserRepository;
import br.com.eduardo.quarkussocial.rest.dto.CreatePostRequest;
import br.com.eduardo.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(
        UserRepository userRepository,
        PostRepository postRepository,
        FollowerRepository followerRepository
    ){
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest request){

        User user = this.userRepository.findById(userId);

        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Post post = new Post();
        post.setPostText(request.getText());
        post.setUser(user);

        postRepository.persist(post);

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public Response listPosts(
        @PathParam("userId") Long userId,
        @HeaderParam("followerId") Long followerId
    ){

        User user = this.userRepository.findById(userId);

        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(followerId == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("You forgot this header followerId")
                    .build();
        }

        var follower = userRepository.findById(followerId);

        if(follower == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nonexistent follower user")
                    .build();
        }

        boolean follows = followerRepository.follows(follower, user);

        if(!follows){
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("You can't see these posts")
                    .build();
        }

        PanacheQuery<Post> query = postRepository.find(
            "user",
            Sort.by("dateTime", Sort.Direction.Descending),
            user
        );

        var list = query.list();

        var result = list.stream()
//                .map(post -> PostResponse.fromEntity(post))
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(result).build();
    }

}
