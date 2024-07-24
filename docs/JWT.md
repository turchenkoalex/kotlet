# JWT Authentication

```kotlin
implementation("com.ecwid:kotlet-jwt:1.0.0")
```

> [!IMPORTANT]
> For using this extension you must add to your
> dependencies [com.auth0:java-jwt](https://mvnrepository.com/artifact/com.auth0/java-jwt) library.

You can use the `installJWTAuthentication` method to add JWT authentication to your routes. The method takes a
`com.auth0.jwt.interfaces.JWTVerifier` object as a parameter, which contains the logic for constructing and validating
JWT tokens.

```kotlin
val jwtVerifier = JWT.require(Algorithm.HMAC256("secret"))
    .withIssuer("myapp")
    .build()

Kotlet.routing {
    installJWTAuthentication(jwtVerifier)
    get("/authentication") { call ->
        val token = call.identity<DecodedJWT>()
        if (token == null) {
            call.respondError(401, "Unauthorized")
            return@get
        }
        call.respondText("Hello, route!")
    }
}
```

> [!TIP]
> Additionally, you can build your own identity object by providing a `identityBuilder: IdentityBuilder<*>` function.

```kotlin
val jwtVerifier = JWT.require(Algorithm.HMAC256("secret"))
    .withIssuer("myapp")
    .build()

data class MyIdentity(val id: String, val admin: Boolean)

fun buildIdentity(token: DecodedJWT): MyIdentity {
    // convert JWT token to MyIdentity object
    return MyIdentity(...)
}

Kotlet.routing {
    installJWTAuthentication(jwtVerifier, ::buildIdentity)
    get("/authentication") { call ->
        val identity = call.identity<MyIdentity>()
        if (identity == null) {
            call.respondError(401, "Unauthorized")
            return@get
        }

        if (!identity.admin) {
            call.respondError(403, "Forbidden")
            return@get
        }

        call.respondText("Hello, admin!")
    }
}
```
