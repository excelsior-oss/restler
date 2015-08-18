package org.restler.integration

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.Callable
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

    RequestMapping("getCallable")
    open fun callableGet(): Callable<String> {
        return Callable (
                fun(): String {
                    Thread.sleep(1000)
                    return "Callable OK"
                }
        );
    }

    RequestMapping("getWithVariable/{title}")
    open fun getWithVariable(@PathVariable(value = "title") title: String, @RequestParam(value = "name") name: String): String {
        return name;
    }

    RequestMapping("throwException")
    @throws(Throwable::class)
    open fun throwException(@RequestParam exceptionClass: String) {
        throw Class.forName(exceptionClass).asSubclass(javaClass<Throwable>()).newInstance()
    }

    @ExceptionHandler(Exception::class)
    open fun printStackTrace(req: HttpServletRequest, e: Exception): String {
        val stringWriter = StringWriter()
        e.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()

    }

    RequestMapping("listOfStrings")
    open fun getListOfStrings() = listOf("item1", "item2")

    private val simpleDto1 = SimpleDto("1", "dto1")

    private val simpleDto2 = SimpleDto("2", "dto2");

    RequestMapping("listOfDtos")
    open fun getListOfDtos() = listOf(simpleDto1, simpleDto2)

    RequestMapping("setOfDtos")
    open fun getSetOfDtos() = setOf(simpleDto1, simpleDto2)

    RequestMapping("mapOfDtos")
    open fun getMapOfDtos() = mapOf("1" to simpleDto1, "2" to simpleDto2)

    RequestMapping("isNull")
    open fun isNull(@RequestParam(required = false) str: String?) = str identityEquals null
}
