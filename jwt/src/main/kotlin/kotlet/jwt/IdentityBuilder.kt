package kotlet.jwt

import com.auth0.jwt.interfaces.DecodedJWT

/**
 * Identity builder
 *
 * This method is used to convert a DecodedJWT to a custom identity,
 * then it can be accessed from the HttpCall, by calling `call.identity<T>()`
 */
typealias IdentityBuilder<T> = (DecodedJWT) -> T
