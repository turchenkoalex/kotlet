package kotlet.jwt

import com.auth0.jwt.interfaces.DecodedJWT

typealias IdentityBuilder<T> = (DecodedJWT) -> T
