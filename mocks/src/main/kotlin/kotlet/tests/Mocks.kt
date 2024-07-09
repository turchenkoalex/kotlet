package kotlet.tests

import io.mockk.every
import io.mockk.mockk
import kotlet.HttpCall
import kotlet.HttpMethod

object Mocks {
    fun mockHttpCall(
        method: HttpMethod,
        headers: Map<String, String>
    ): HttpCall {
        val attributes = mutableMapOf<String, Any>()

        return mockk {
            every { rawRequest } returns mockk {
                every { httpMethod } returns method
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

}
