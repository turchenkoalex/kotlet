import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.receiveBody
import kotlet.receivePath
import kotlet.respondJson
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Serializable
data class Post(val id: Long, val title: String, val description: String)

@Serializable
data class Posts(val items: List<Post>)

@Serializable
data class CreatePostRequest(val title: String, val description: String?)

data class PostIdQuery(val id: Long)

@Serializable
data class UpdatePostRequest(val title: String?, val description: String?)

class PostsService {
    private val storage: MutableMap<Long, Post> = ConcurrentHashMap()
    private val idGenerator = AtomicLong(1)

    init {
        storage[1] = Post(
            id = 1,
            title = "First post",
            description = "Description of first post",
        )
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
        )
        storage[newId] = post
        httpCall.status = HttpServletResponse.SC_CREATED
    }

    fun get(httpCall: HttpCall) {
        val postIdQuery = httpCall.receivePath<PostIdQuery>()
        val post = storage[postIdQuery.id]
        if (post == null) {
            httpCall.respondError(HttpServletResponse.SC_NOT_FOUND, "Post ${postIdQuery.id} not found")
            return
        }
        httpCall.respondJson(post)
    }

    fun update(httpCall: HttpCall) {
        val postIdQuery = httpCall.receivePath<PostIdQuery>()
        val updateRequest = httpCall.receiveBody<UpdatePostRequest>()
        val existsPost = storage[postIdQuery.id]
        if (existsPost == null) {
            httpCall.respondError(HttpServletResponse.SC_NOT_FOUND, "Post ${postIdQuery.id} not found")
            return
        }

        val newPost = Post(
            id = postIdQuery.id,
            title = updateRequest.title ?: existsPost.title,
            description = updateRequest.description ?: existsPost.description,
        )
        storage[postIdQuery.id] = newPost
        httpCall.status = HttpServletResponse.SC_NO_CONTENT
    }

    fun delete(httpCall: HttpCall) {
        val postIdQuery = httpCall.receivePath<PostIdQuery>()
        val exists = storage.contains(postIdQuery.id)
        if (!exists) {
            httpCall.respondError(HttpServletResponse.SC_NOT_FOUND, "Post ${postIdQuery.id} not found")
            return
        }

        storage.remove(postIdQuery.id)
        httpCall.status = HttpServletResponse.SC_NO_CONTENT
    }
}