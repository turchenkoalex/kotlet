package kotlet.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import kotlet.HttpMethod
import kotlet.mocks.Interceptors
import kotlet.mocks.Mocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class JWTAuthenticationInterceptorUnitTest {
    @Test
    fun `interceptor should add identity to call and remove it afterCall`() {
        val verifier = JWT.require(Algorithm.none()).build()
        val interceptor = JWTAuthenticationInterceptor(verifier) { it }

        val jwtToken = JWT.create().withIssuer("test")
            .withClaim("scope", "test")
            .sign(Algorithm.none())

        val call = Mocks.mockHttpCall(
            method = HttpMethod.GET,
            headers = mapOf("Authorization" to "Bearer $jwtToken")
        )

        var token: DecodedJWT? = null
        Interceptors.invokeInterceptor(interceptor, call) {
            token = call.identity<DecodedJWT>()
        }

        assertNull(call.identity<DecodedJWT>())

        val actual = token
        assertNotNull(actual)
        assertEquals("test", actual.getClaim("scope").asString())
    }

    @Test
    fun `interceptor should convert identity to call and remove it afterCall`() {
        val verifier = JWT.require(Algorithm.none()).build()
        val interceptor = JWTAuthenticationInterceptor(verifier) {
            TestIdentity(scope = it.getClaim("scope").asString())
        }

        val jwtToken = JWT.create().withIssuer("test")
            .withClaim("scope", "test")
            .sign(Algorithm.none())

        val call = Mocks.mockHttpCall(
            method = HttpMethod.GET,
            headers = mapOf("Authorization" to "Bearer $jwtToken")
        )

        var identity: TestIdentity? = null
        Interceptors.invokeInterceptor(interceptor, call) {
            identity = call.identity<TestIdentity>()
        }

        val actual = identity
        assertNotNull(identity)
        assertEquals(TestIdentity(scope = "test"), actual)
    }

    private data class TestIdentity(val scope: String)
}
