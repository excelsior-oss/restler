package org.restler.testserver

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import kotlin.concurrent.thread

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

    RequestMapping("getDeferred")
    open fun deferredGet(): DeferredResult<String> {
        var deferredResult = DeferredResult<String>();

        thread {
            Thread.sleep(1000)
            deferredResult.setResult("Deferred OK");
        }

        return deferredResult;
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
