package org.restler.testserver

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KClass

RestController
public open class Controller {

    RequestMapping("get")
    open fun publicGet() = "OK"

    RequestMapping("secured/get")
    open fun securedGet() = "Secure OK"

    RequestMapping("forceLogout")
    open fun logout(): String {
        SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false)
        return "OK"
    }

    RequestMapping("throwException")
    @throws(Throwable::class)
    open fun throwException(@RequestParam exceptionClass: String) {
        throw Class.forName(exceptionClass).asSubclass(javaClass<Throwable>()).newInstance()
    }

    @ExceptionHandler(Exception::class)
    fun printStackTrace(req: HttpServletRequest, e: Exception): String {
        val stringWriter = StringWriter()
        e.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()

    }
}
