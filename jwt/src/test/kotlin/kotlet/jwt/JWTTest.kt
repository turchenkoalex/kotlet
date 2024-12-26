package kotlet.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlet.Kotlet.routing
import kotlin.test.Test
import kotlin.test.assertEquals

class JWTTest {
    @Test
    fun shouldRegisterJWTInterceptorsForRoutes() {
        val verifier = JWT.require(Algorithm.none()).build()
        val routes = routing {
            useJWTAuthentication(verifier) {
                get("/a") {}
                get("/b") {}
            }
        }

        val countOfRoutesWithJWT = routes.registeredRoutes.sumOf { route ->
            route.interceptors.count {
                it is JWTAuthenticationInterceptor
            }
        }

        assertEquals(2, countOfRoutesWithJWT, "Expected 2 routes with JWT interceptor, but found $countOfRoutesWithJWT")
    }


}
