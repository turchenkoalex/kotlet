package kotlet.http

/**
 * Exception thrown when a method is not allowed. For internal use only.
 */
internal class MethodNotAllowedException : RuntimeException("Method not allowed")