package posts

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long,
    val title: String,
    val description: String,
    val author: Author? = null,
    val comments: List<Comment>,
    val status: Status
)

@Serializable
data class Author(val id: Long, val name: String)

@Serializable
data class Comment(val id: Long, val text: String, val date: Map<String, Author>)

@Serializable
data class Posts(val items: List<Post>)

@Serializable
data class CreatePostRequest(val title: String, val description: String?)

data class PostIdQuery(
    val id: Long
)

@Serializable
data class UpdatePostRequest(val title: String?, val description: String?)

enum class Status {
    DRAFT,
    PUBLISHED
}
