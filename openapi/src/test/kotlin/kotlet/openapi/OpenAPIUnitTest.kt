package kotlet.openapi

import kotlet.HttpMethod
import kotlet.Kotlet
import kotlet.Routing
import kotlet.mocks.Mocks
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Instant
import kotlin.test.Test


class OpenAPIUnitTest {
    @Test
    fun `should return OpenAPI without any methods`() {
        val routing = Kotlet.routing {
            get("/posts/{id}", Mocks.okHandler)
        }

        assertOpenAPIJson(
            """
            {
              "openapi" : "3.0.1",
              "info" : {
                "title" : "Sample API",
                "version" : "1.0"
              }
            }
            """.trimIndent(),
            routing
        )
    }

    @Test
    fun `should return OpenAPI with one method`() {
        val routing = Kotlet.routing {
            get("/posts/{id}", Mocks.okHandler) {
                openapi {
                    summary("Get post by ID")
                    parameters {
                        path<Long>("id") {
                            description = "Post ID"
                        }
                    }
                    responses {
                        jsonResponse<Post>("Post found", statusCode = 200)
                        notFound("Post not found")
                    }
                }
            }
        }

        assertOpenAPIJson(
            """
            {
              "openapi" : "3.0.1",
              "info" : {
                "title" : "Sample API",
                "version" : "1.0"
              },
              "paths" : {
                "/posts/{id}" : {
                  "get" : {
                    "summary" : "Get post by ID",
                    "parameters" : [ {
                      "name" : "id",
                      "in" : "path",
                      "description" : "Post ID",
                      "required" : true,
                      "schema" : {
                        "type" : "integer",
                        "format" : "int64"
                      }
                    } ],
                    "responses" : {
                      "200" : {
                        "description" : "Post found",
                        "content" : {
                          "application/json" : {
                            "schema" : {
                              "type" : "object",
                              "properties" : {
                                "comments" : {
                                  "type" : "object"
                                },
                                "content" : {
                                  "type" : "string",
                                  "nullable" : true
                                },
                                "id" : {
                                  "type" : "integer",
                                  "format" : "int32"
                                },
                                "title" : {
                                  "type" : "string"
                                }
                              },
                              "description" : "kotlet.openapi.Post"
                            }
                          }
                        }
                      },
                      "404" : {
                        "description" : "Post not found"
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent(),
            routing
        )
    }

    private fun assertOpenAPIJson(expectedJson: String, routing: Routing) {
        val openAPIRouting = Kotlet.routing {
            installOpenAPI {
                path = "/swagger/openapi.json"
                this.documentedRoutings = listOf(routing)
                prettyPrint = true
                openAPI {
                    info {
                        title = "Sample API"
                        version = "1.0"
                    }

                }
            }
        }

        val kotlet = Kotlet.servlet(
            routings = listOf(routing, openAPIRouting),
        )

        val call = Mocks.httpCall(
            method = HttpMethod.GET,
            routePath = "/swagger/openapi.json",
        )
        kotlet.service(call.rawRequest, call.rawResponse)

        val response = call.responseData

        assertEquals(expectedJson, response.toString(Charsets.UTF_8))
    }
}

private data class Post(val id: Int, val title: String, val content: String?, val comments: Map<Instant, Comment>)
private data class Comment(val id: Int, val text: String)
