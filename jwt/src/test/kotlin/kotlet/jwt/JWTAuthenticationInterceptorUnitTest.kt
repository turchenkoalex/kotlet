package kotlet.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.mockk.every
import io.mockk.mockk
import kotlet.Handler
import kotlet.HttpCall
import kotlet.HttpMethod
import kotlet.Interceptor
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

        val call = mockCall(
            headers = mapOf("Authorization" to "Bearer $jwtToken")
        )

        var token: DecodedJWT? = null
        invokeInterceptor(interceptor, call) {
            token = call.identity<DecodedJWT>()
        }

        assertNull(call.identity<DecodedJWT>())

        val actual = token
        assertNotNull(actual)
        assertEquals("test", actual.getClaim("scope").asString())
    }

    @Test
    fun `interceptor should covert identity to call and remove it afterCall`() {
        val verifier = JWT.require(Algorithm.none()).build()
        val interceptor = JWTAuthenticationInterceptor(verifier) {
            TestIdentity(scope = it.getClaim("scope").asString())
        }

        val jwtToken = JWT.create().withIssuer("test")
            .withClaim("scope", "test")
            .sign(Algorithm.none())

        val call = mockCall(
            headers = mapOf("Authorization" to "Bearer $jwtToken")
        )

        var identity: TestIdentity? = null
        invokeInterceptor(interceptor, call) {
            identity = call.identity<TestIdentity>()
        }

        val actual = identity
        assertNotNull(identity)
        assertEquals(TestIdentity(scope = "test"), actual)
    }

    private data class TestIdentity(val scope: String)
}

private fun mockCall(headers: Map<String, String> = emptyMap()): HttpCall {
    val attributes = mutableMapOf<String, Any>()

    return mockk {
        every { rawRequest } returns mockk {
            every { httpMethod } returns HttpMethod.GET
            every { getHeader(any()) } answers {
                headers[this.firstArg()]
            }
            every { getAttribute(any()) } answers {
                attributes[this.firstArg()]
            }
            every { setAttribute(any(), any()) } answers {
                attributes[this.firstArg()] = this.secondArg()
            }
            every { removeAttribute(any()) } answers {
                attributes.remove(this.firstArg())
            }
            every { isAsyncStarted } returns false
        }
    }
}

private fun invokeInterceptor(interceptor: Interceptor, call: HttpCall, handler: Handler) {
    val newCall = interceptor.beforeCall(call)
    interceptor.aroundCall(newCall, handler)
    interceptor.afterCall(newCall)
}
