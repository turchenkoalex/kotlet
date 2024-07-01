package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object Auth {
    fun createVerifier(): com.auth0.jwt.JWTVerifier {
        val algorithm = Algorithm.HMAC256("SecretString")
        return JWT.require(algorithm)
            .withIssuer("myapp")
            .build()
    }
}
