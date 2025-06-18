package kotlet.mocks.http

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import java.io.OutputStream

/**
 * A [ServletOutputStream] that writes to an [OutputStream].
 */
internal class ByteArrayServletOutputStream(
    private val outputStream: OutputStream
) : ServletOutputStream() {
    override fun write(b: Int) {
        outputStream.write(b)
    }

    override fun write(b: ByteArray) {
        outputStream.write(b)
    }

    override fun isReady(): Boolean {
        return true
    }

    override fun setWriteListener(writeListener: WriteListener) {
        writeListener.onWritePossible()
    }
}
