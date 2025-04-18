# Type-Safe request objects

## Installation

```kotlin
implementation("io.github.turchenkoalex:kotlet-typesafe:0.24.0")
```

## Configuration

Few helpful methods for parsing request query parameters: `receivePath` and `receiveQuery`.

```kotlin
@Serializable
data class UserPath(val name: String)

@Serializable
data class UserQuery(val age: Int, val city: String)

post("/user/{name?}") { call ->
    // /user/John?age=30&city=New York
    val user = call.receivePath<UserPath>() // UserPath(name = "John")
    val query = call.receiveQuery<UserQuery>() // UserQuery(age = 30, city = "New York")
    update(user, query)
    call.respondJson(user)
}
```
