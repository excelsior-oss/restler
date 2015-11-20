package org.restler.integration

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.Callable
import java.util.concurrent.Future
import kotlin.concurrent.thread

@RestController
public open class Controller {

    @RequestMapping("get")
    open fun publicGet() = "OK"

    @RequestMapping("secured/get")
    open fun securedGet() = "Secure OK"

    @RequestMapping("forceLogout")
    open fun logout(): String {
        SecurityContextHolder.getContext().authentication.isAuthenticated = false
        return "OK"
    }

    @RequestMapping("getDeferred")
    open fun deferredGet(): DeferredResult<String> {
        var deferredResult = DeferredResult<String>()

        thread {
            Thread.sleep(1000)
            deferredResult.setResult("Deferred OK")
        }

        return deferredResult
    }

    @RequestMapping("getCallable")
    open fun callableGet(): Callable<String> {
        return Callable (
                fun(): String {
                    Thread.sleep(1000)
                    return "Callable OK"
                }
        )
    }

    @Async
    @RequestMapping("getFuture")
    open fun futureGet(): Future<String> {
        Thread.sleep(1000)
        return AsyncResult<String>("Future OK")
    }

    @RequestMapping("getWithVariable/{title}")
    open fun getWithVariable(@PathVariable(value = "title") title: String, @RequestParam(value = "name") name: String): String {
        return name
    }

    @RequestMapping("throwException")
    @Throws(Throwable::class)
    open fun throwException(@RequestParam exceptionClass: String): String {
        throw Class.forName(exceptionClass).asSubclass(Throwable::class.java).newInstance()
    }

    @RequestMapping("listOfStrings")
    open fun getListOfStrings() = listOf("item1", "item2")

    private val simpleDto1 = SimpleDto("1", "dto1")

    private val simpleDto2 = SimpleDto("2", "dto2")

    @RequestMapping("listOfDtos")
    open fun getListOfDtos() = listOf(simpleDto1, simpleDto2)

    @RequestMapping("setOfDtos")
    open fun getSetOfDtos() = setOf(simpleDto1, simpleDto2)

    @RequestMapping("mapOfDtos")
    open fun getMapOfDtos() = mapOf("1" to simpleDto1, "2" to simpleDto2)

    @RequestMapping("isNull")
    open fun isNull(@RequestParam(required = false) str: String?) = str === null

    @RequestMapping("valueOf")
    open fun valueOf(@RequestParam(required = false) str: String?) = when (str) {
        null -> "The Null"
        "" -> "Empty string object"
        else -> "String object with value: $str"
    }
}
