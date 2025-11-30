package kotlet.openapi.dsl

import io.swagger.v3.oas.models.Operation

var Operation.tags: List<String>
    get() = this.tags
    set(value) {
        this.tags = value.toList()
    }
