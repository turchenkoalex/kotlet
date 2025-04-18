package kotlet.jwt

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import jakarta.servlet.AsyncListener
import kotlet.HttpCall
import kotlet.Interceptor

private const val AUTHORIZATION_HEADER = "Authorization"
private const val BEARER_PREFIX = "Bearer "
internal const val IDENTITY_PARAMETER_NAME = "kotlet.jwt.identity"

internal class JWTAuthenticationInterceptor(
    private val verifier: JWTVerifier,
    private val identityBuilder: IdentityBuilder<*>
) : Interceptor {
    override fun beforeCall(call: HttpCall): HttpCall {
        val header = call.rawRequest.getHeader(AUTHORIZATION_HEADER)
            ?: return call

        if (!header.startsWith(BEARER_PREFIX)) {
            return call
        }

        val token = header.substring(BEARER_PREFIX.length)
        if (token.isEmpty()) {
            return call
        }

        try {
            val decodedToken = verifier.verify(token)
            call.rawRequest.setAttribute(IDENTITY_PARAMETER_NAME, identityBuilder(decodedToken))
        } catch (_: JWTVerificationException) {
            // verification failed
        }

        return call
    }

    override fun afterCall(call: HttpCall): HttpCall {
        if (call.rawRequest.isAsyncStarted) {
            call.rawRequest.asyncContext.addListener(JWTAttributeAsyncCleaner)
        } else {
            call.rawRequest.removeAttribute(IDENTITY_PARAMETER_NAME)
        }
        return call
    }
}

private object JWTAttributeAsyncCleaner : AsyncListener {
    override fun onComplete(event: jakarta.servlet.AsyncEvent) {
        event.asyncContext.request.removeAttribute(IDENTITY_PARAMETER_NAME)
    }

    override fun onTimeout(event: jakarta.servlet.AsyncEvent) {
        event.asyncContext.request.removeAttribute(IDENTITY_PARAMETER_NAME)
    }

    override fun onError(event: jakarta.servlet.AsyncEvent) {
        event.asyncContext.request.removeAttribute(IDENTITY_PARAMETER_NAME)
    }

    override fun onStartAsync(event: jakarta.servlet.AsyncEvent) {
    }
}

