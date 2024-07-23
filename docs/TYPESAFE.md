# Type-Safe request objects

```kotlin
implementation("com.ecwid:kotlet-typesafe:1.0.0")
```

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
