package org.restler.integration

import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.Callable
import java.util.concurrent.Future

@RestController
interface ControllerApi {

    @RequestMapping("get")
    open fun publicGet(): String

    @RequestMapping("secured/get")
    open fun securedGet(): String

    @RequestMapping("forceLogout")
    open fun logout(): String

    @RequestMapping("getCallable")
    open fun callableGet(): Callable<String>

    @Async
    @RequestMapping("getFuture")
    open fun futureGet(): Future<String>

    @RequestMapping("getWithVariable/{title}")
    open fun getWithVariable(@PathVariable(value = "title") title: String, @RequestParam(value = "name") name: String): String

    @RequestMapping("console")
    open fun console(@RequestParam(value = "text") text: String)

    @RequestMapping("throwException")
    @Throws(Throwable::class)
    open fun throwException(@RequestParam(value = "exceptionClass") exceptionClass: String): String

    @RequestMapping("listOfStrings")
    open fun getListOfStrings(): List<String>

    @RequestMapping("listOfDtos")
    open fun getListOfDtos(): List<SimpleDto>

    @RequestMapping("setOfDtos")
    open fun getSetOfDtos(): Set<SimpleDto>

    @RequestMapping("mapOfDtos")
    open fun getMapOfDtos(): Map<String, SimpleDto>

    @RequestMapping("isNull")
    open fun isNull(@RequestParam(value = "str", required = false) str: String?): Boolean

    @RequestMapping("valueOf")
    open fun valueOf(@RequestParam(value = "str", required = false) str: String?): String

    @RequestMapping("void")
    open fun returnVoid()

    @RequestMapping("postBody", method = arrayOf(RequestMethod.POST))
    open fun postBody(@RequestBody() body: Any): Any

    @RequestMapping(method = arrayOf(RequestMethod.POST), value = "/upload")
    fun fileUpload(@RequestParam("name") name: String, @RequestParam("file") file: MultipartFile): String
}
