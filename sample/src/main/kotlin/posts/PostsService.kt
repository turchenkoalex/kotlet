package posts

import auth.Scope
import auth.requiredScopes
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.Routing
import kotlet.openapi.jsonRequest
import kotlet.openapi.notFound
import kotlet.openapi.jsonResponse
import kotlet.openapi.openapi
import kotlet.openapi.parameters
import kotlet.openapi.responses
import kotlet.openapi.tags
import kotlet.receiveBody
import kotlet.receivePath
import kotlet.respondJson
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class PostsService {
    private val storage: MutableMap<Long, Post> = ConcurrentHashMap()
    private val idGenerator = AtomicLong(1)

    init {
        storage[1] = Post(
            id = 1,
            title = "First post",
            description = "Description of first post",
            author = Author(
                id = 1,
                name = "John Doe"
            ),
            comments = listOf(
                Comment(
                    id = 1,
                    text = "First comment",
                    date = mapOf("date" to Author(1, "John Doe"))
                )
            ),
            status = Status.DRAFT,
        )
    }

    fun installRoutes(routing: Routing) = with(routing) {

        requiredScopes(Scope.READ_POSTS) {
            get("/", ::list) {
                openapi {
                    summary("List posts")
                    tags("posts")
                    responses {
                        jsonResponse<Posts>("Posts found")
                    }
                }
            }
            get("/{id}", ::get) {
                openapi {
                    summary("Get post by ID")
                    tags("posts")
                    parameters<PostIdQuery>()
                    responses {
                        jsonResponse<Post>("Post found")
                        notFound("Post not found")
                    }
                }
            }
        }

        requiredScopes(Scope.WRITE_POSTS) {
            post("/", ::create) {
                openapi {
                    summary("Create a post")
                    tags("posts")
                    jsonRequest<CreatePostRequest>()
                }
            }
            put("/{id}", ::update) {
                openapi {
                    summary("Update post by ID")
                    tags("posts")
                    parameters<PostIdQuery>()
                    jsonRequest<UpdatePostRequest>()
                }
            }
        }

        requiredScopes(Scope.DELETE_POSTS) {
            delete("/{id}", ::delete) {
                openapi {
                    summary("Delete post by ID")
                    tags("posts")
                    parameters<PostIdQuery>()
                }
            }
        }
    }

    fun list(httpCall: HttpCall) {
        val posts = Posts(storage.values.toList())
        httpCall.respondJson(posts)
    }

    fun create(httpCall: HttpCall) {
        val reqBody = httpCall.receiveBody<CreatePostRequest>()
        val newId = idGenerator.incrementAndGet()
        val post = Post(
            id = newId,
            title = reqBody.title,
            description = reqBody.description ?: "",
            author = null,
            comments = emptyList(),
            status = Status.DRAFT,
        )
        storage[newId] = post
        httpCall.status = HttpServletResponse.SC_CREATED
    }

    fun get(httpCall: HttpCall) {
        val postIdQuery = httpCall.receivePath<PostIdQuery>()
        val post = storage[postIdQuery.id]
        if (post == null) {
            httpCall.respondError(HttpServletResponse.SC_NOT_FOUND, "posts.Post ${postIdQuery.id} not found")
            return
        }
        httpCall.respondJson(post)
    }

    fun update(httpCall: HttpCall) {
        val postIdQuery = httpCall.receivePath<PostIdQuery>()
        val updateRequest = httpCall.receiveBody<UpdatePostRequest>()
        val existsPost = storage[postIdQuery.id]
        if (existsPost == null) {
            httpCall.respondError(HttpServletResponse.SC_NOT_FOUND, "posts.Post ${postIdQuery.id} not found")
            return
        }

        val newPost = Post(
            id = postIdQuery.id,
            title = updateRequest.title ?: existsPost.title,
            description = updateRequest.description ?: existsPost.description,
            author = existsPost.author,
            comments = existsPost.comments,
            status = existsPost.status,
        )
        storage[postIdQuery.id] = newPost
        httpCall.status = HttpServletResponse.SC_NO_CONTENT
    }

    fun delete(httpCall: HttpCall) {
        val postIdQuery = httpCall.receivePath<PostIdQuery>()
        val exists = storage.contains(postIdQuery.id)
        if (!exists) {
            httpCall.respondError(HttpServletResponse.SC_NOT_FOUND, "posts.Post ${postIdQuery.id} not found")
            return
        }

        storage.remove(postIdQuery.id)
        httpCall.status = HttpServletResponse.SC_NO_CONTENT
    }
}
