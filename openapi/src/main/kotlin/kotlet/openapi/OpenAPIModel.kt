package kotlet.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info

fun OpenAPI.info(configure: Info.() -> Unit) {
    if (info == null) {
        info = Info()
    }
    info.configure()
}
