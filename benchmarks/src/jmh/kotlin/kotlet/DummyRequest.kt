package kotlet

import jakarta.servlet.AsyncContext
import jakarta.servlet.DispatcherType
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.ServletConnection
import jakarta.servlet.ServletContext
import jakarta.servlet.ServletInputStream
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpUpgradeHandler
import jakarta.servlet.http.Part
import java.io.BufferedReader
import java.security.Principal
import java.util.Enumeration
import java.util.Locale

class DummyRequest(val path: String) : HttpServletRequest {
    override fun getAuthType(): String? {
        TODO("Not yet implemented")
    }

    override fun getCookies(): Array<out Cookie?>? {
        TODO("Not yet implemented")
    }

    override fun getDateHeader(name: String?): Long {
        TODO("Not yet implemented")
    }

    override fun getHeader(name: String?): String? {
        TODO("Not yet implemented")
    }

    override fun getHeaders(name: String?): Enumeration<String?>? {
        TODO("Not yet implemented")
    }

    override fun getHeaderNames(): Enumeration<String?>? {
        TODO("Not yet implemented")
    }

    override fun getIntHeader(name: String?): Int {
        TODO("Not yet implemented")
    }

    override fun getMethod(): String? {
        TODO("Not yet implemented")
    }

    override fun getPathInfo(): String? {
        TODO("Not yet implemented")
    }

    override fun getPathTranslated(): String? {
        TODO("Not yet implemented")
    }

    override fun getContextPath(): String? {
        TODO("Not yet implemented")
    }

    override fun getQueryString(): String? {
        TODO("Not yet implemented")
    }

    override fun getRemoteUser(): String? {
        TODO("Not yet implemented")
    }

    override fun isUserInRole(role: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getUserPrincipal(): Principal? {
        TODO("Not yet implemented")
    }

    override fun getRequestedSessionId(): String? {
        TODO("Not yet implemented")
    }

    override fun getRequestURI(): String? {
        return path
    }

    override fun getRequestURL(): StringBuffer? {
        TODO("Not yet implemented")
    }

    override fun getServletPath(): String? {
        TODO("Not yet implemented")
    }

    override fun getSession(create: Boolean): HttpSession? {
        TODO("Not yet implemented")
    }

    override fun getSession(): HttpSession? {
        TODO("Not yet implemented")
    }

    override fun changeSessionId(): String? {
        TODO("Not yet implemented")
    }

    override fun isRequestedSessionIdValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isRequestedSessionIdFromCookie(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isRequestedSessionIdFromURL(): Boolean {
        TODO("Not yet implemented")
    }

    override fun authenticate(response: HttpServletResponse?): Boolean {
        TODO("Not yet implemented")
    }

    override fun login(username: String?, password: String?) {
        TODO("Not yet implemented")
    }

    override fun logout() {
        TODO("Not yet implemented")
    }

    override fun getParts(): Collection<Part?>? {
        TODO("Not yet implemented")
    }

    override fun getPart(name: String?): Part? {
        TODO("Not yet implemented")
    }

    override fun <T : HttpUpgradeHandler?> upgrade(handlerClass: Class<T?>?): T? {
        TODO("Not yet implemented")
    }

    override fun getAttribute(name: String?): Any? {
        TODO("Not yet implemented")
    }

    override fun getAttributeNames(): Enumeration<String?>? {
        TODO("Not yet implemented")
    }

    override fun getCharacterEncoding(): String? {
        TODO("Not yet implemented")
    }

    override fun setCharacterEncoding(encoding: String?) {
        TODO("Not yet implemented")
    }

    override fun getContentLength(): Int {
        TODO("Not yet implemented")
    }

    override fun getContentLengthLong(): Long {
        TODO("Not yet implemented")
    }

    override fun getContentType(): String? {
        TODO("Not yet implemented")
    }

    override fun getInputStream(): ServletInputStream? {
        TODO("Not yet implemented")
    }

    override fun getParameter(name: String?): String? {
        TODO("Not yet implemented")
    }

    override fun getParameterNames(): Enumeration<String?>? {
        TODO("Not yet implemented")
    }

    override fun getParameterValues(name: String?): Array<out String?>? {
        TODO("Not yet implemented")
    }

    override fun getParameterMap(): Map<String?, Array<out String?>?>? {
        TODO("Not yet implemented")
    }

    override fun getProtocol(): String? {
        TODO("Not yet implemented")
    }

    override fun getScheme(): String? {
        TODO("Not yet implemented")
    }

    override fun getServerName(): String? {
        TODO("Not yet implemented")
    }

    override fun getServerPort(): Int {
        TODO("Not yet implemented")
    }

    override fun getReader(): BufferedReader? {
        TODO("Not yet implemented")
    }

    override fun getRemoteAddr(): String? {
        TODO("Not yet implemented")
    }

    override fun getRemoteHost(): String? {
        TODO("Not yet implemented")
    }

    override fun setAttribute(name: String?, o: Any?) {
        TODO("Not yet implemented")
    }

    override fun removeAttribute(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getLocale(): Locale? {
        TODO("Not yet implemented")
    }

    override fun getLocales(): Enumeration<Locale?>? {
        TODO("Not yet implemented")
    }

    override fun isSecure(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher? {
        TODO("Not yet implemented")
    }

    override fun getRemotePort(): Int {
        TODO("Not yet implemented")
    }

    override fun getLocalName(): String? {
        TODO("Not yet implemented")
    }

    override fun getLocalAddr(): String? {
        TODO("Not yet implemented")
    }

    override fun getLocalPort(): Int {
        TODO("Not yet implemented")
    }

    override fun getServletContext(): ServletContext? {
        TODO("Not yet implemented")
    }

    override fun startAsync(): AsyncContext? {
        TODO("Not yet implemented")
    }

    override fun startAsync(
        servletRequest: ServletRequest?,
        servletResponse: ServletResponse?
    ): AsyncContext? {
        TODO("Not yet implemented")
    }

    override fun isAsyncStarted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAsyncSupported(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAsyncContext(): AsyncContext? {
        TODO("Not yet implemented")
    }

    override fun getDispatcherType(): DispatcherType? {
        TODO("Not yet implemented")
    }

    override fun getRequestId(): String? {
        TODO("Not yet implemented")
    }

    override fun getProtocolRequestId(): String? {
        TODO("Not yet implemented")
    }

    override fun getServletConnection(): ServletConnection? {
        TODO("Not yet implemented")
    }

}
