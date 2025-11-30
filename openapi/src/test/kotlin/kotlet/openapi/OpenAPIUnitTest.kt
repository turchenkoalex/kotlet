package kotlet.openapi

import kotlet.HttpMethod
import kotlet.Kotlet
import kotlet.Routing
import kotlet.mocks.Mocks
import kotlet.openapi.dsl.info
import kotlet.openapi.dsl.jsonRequest
import kotlet.openapi.dsl.jsonResponse
import kotlet.openapi.dsl.notFound
import kotlet.openapi.dsl.parameters
import kotlet.openapi.dsl.pathParameter
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Instant
import kotlin.test.Test


class OpenAPIUnitTest {
    @Test
    fun `should return OpenAPI without any methods`() {
        val routing = Kotlet.routing {
            openAPI("/swagger/openapi.json") {
                prettyPrint = true
                describe {
                    info {
                        title = "Sample API"
                        version = "1.0"
                    }
                }
            }

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
            openAPI("/swagger/openapi.json") {
                prettyPrint = true
                describe {
                    info {
                        title = "Sample API"
                        version = "1.0"
                    }
                }
            }

            get("/posts/{id}", Mocks.okHandler) describe {
                summary = "Get post by ID"
                pathParameter<Long>("id", description = "Post ID")
                jsonResponse<Post>(200, "Post found")
                notFound("Post not found")
            }

            post("/post/{postId}/comments/{commentId}") { call ->
                call.status = 201
            } describe {
                parameters<CommentPostParameter>()
                jsonRequest<CommentPostRequest>("Comment update body")
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
                                  "type" : "object",
                                  "additionalProperties" : {
                                    "type" : "object",
                                    "properties" : {
                                      "id" : {
                                        "type" : "integer",
                                        "format" : "int32"
                                      },
                                      "text" : {
                                        "type" : "string"
                                      }
                                    },
                                    "description" : "kotlet.openapi.Comment"
                                  }
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
                },
                "/post/{postId}/comments/{commentId}" : {
                  "post" : {
                    "parameters" : [ {
                      "name" : "commentId",
                      "in" : "path",
                      "description" : "",
                      "required" : true,
                      "schema" : {
                        "type" : "integer",
                        "format" : "int32"
                      }
                    }, {
                      "name" : "postId",
                      "in" : "path",
                      "description" : "",
                      "required" : true,
                      "schema" : {
                        "type" : "integer",
                        "format" : "int32"
                      }
                    } ],
                    "requestBody" : {
                      "description" : "Comment update body",
                      "content" : {
                        "application/json" : {
                          "schema" : {
                            "type" : "object",
                            "properties" : {
                              "message" : {
                                "type" : "integer",
                                "format" : "int32"
                              }
                            },
                            "description" : "kotlet.openapi.CommentPostRequest"
                          }
                        }
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
        val kotlet = Kotlet.handler(routing)

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
private data class CommentPostRequest(val message: Int)
private data class CommentPostParameter(val postId: Int, val commentId: Int)
