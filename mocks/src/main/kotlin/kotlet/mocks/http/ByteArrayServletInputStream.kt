package kotlet.mocks.http

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream

/**
 * A [ServletInputStream] that reads from a [ByteArray].
 */
internal class ByteArrayServletInputStream(
    private val data: ByteArray
) : ServletInputStream() {
    private var position: Int = 0
    private var readListener: ReadListener? = null

    override fun read(): Int {
        if (isFinished) {
            return -1
        }

        val byte = data[position++].toInt()

        if (isFinished) {
            readListener?.onAllDataRead()
        }

        return byte
    }

    override fun isFinished(): Boolean {
        return position >= data.size
    }

    override fun isReady(): Boolean {
        return true
    }

    override fun setReadListener(readListener: ReadListener?) {
        this.readListener = readListener
        if (isFinished) {
            readListener?.onAllDataRead()
        } else {
            readListener?.onDataAvailable()
        }
    }
}
