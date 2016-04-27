package org.restler.integration

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.stream.Collectors
import kotlin.concurrent.thread

@RestController
open class Controller : ControllerApi {

    var filesDirectory: String;

    init {
        var root = File("").canonicalPath
        filesDirectory = "$root/files";
        var directory = File(filesDirectory)

        if(!(directory.exists() && directory.isDirectory)) {
            directory.mkdir()
        }
    }

    @RequestMapping("get")
    override fun publicGet() = "OK"

    @RequestMapping("secured/get")
    override fun securedGet() = "Secure OK"

    @RequestMapping("forceLogout")
    override fun logout(): String {
        SecurityContextHolder.getContext().authentication.isAuthenticated = false
        return "OK"
    }

    @RequestMapping("getDeferred")
    fun deferredGet(): DeferredResult<String> {
        var deferredResult = DeferredResult<String>()

        thread {
            Thread.sleep(1000)
            deferredResult.setResult("Deferred OK")
        }

        return deferredResult
    }

    @RequestMapping("getCallable")
    override fun callableGet(): Callable<String> {
        return Callable (
                fun(): String {
                    Thread.sleep(1000)
                    return "Callable OK"
                }
        )
    }

    @Async
    @RequestMapping("getFuture")
    override fun futureGet(): Future<String> {
        Thread.sleep(1000)
        return AsyncResult<String>("Future OK")
    }

    @RequestMapping("getWithVariable/{title}")
    override fun getWithVariable(@PathVariable(value = "title") title: String, @RequestParam(value = "name") name: String): String {
        return name
    }

    @RequestMapping("console")
    override fun console(@RequestParam(value = "text") text: String) {
        System.out.println(text);
    }

    @RequestMapping("throwException")
    @Throws(Throwable::class)
    override fun throwException(@RequestParam exceptionClass: String): String {
        throw Class.forName(exceptionClass).asSubclass(Throwable::class.java).newInstance()
    }

    @RequestMapping("listOfStrings")
    override fun getListOfStrings() = listOf("item1", "item2")

    private val simpleDto1 = SimpleDto("1", "dto1")

    private val simpleDto2 = SimpleDto("2", "dto2")

    @RequestMapping("listOfDtos")
    override fun getListOfDtos() = listOf(simpleDto1, simpleDto2)

    @RequestMapping("setOfDtos")
    override fun getSetOfDtos() = setOf(simpleDto1, simpleDto2)

    @RequestMapping("mapOfDtos")
    override fun getMapOfDtos() = mapOf("1" to simpleDto1, "2" to simpleDto2)

    @RequestMapping("isNull")
    override fun isNull(@RequestParam(value = "str", required = false) str: String?) = str === null

    @RequestMapping("valueOf")
    override fun valueOf(@RequestParam(value = "str", required = false) str: String?) = when (str) {
        null -> "The Null"
        "" -> "Empty string object"
        else -> "String object with value: $str"
    }

    @RequestMapping("void")
    override fun returnVoid() {}

    @RequestMapping("postBody", method = arrayOf(RequestMethod.POST))
    override fun postBody(@RequestBody body: Any) = body

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/upload")
    fun provideUploadInfo(): List<String> {
        var filesFolder = File("$filesDirectory/");
        var fileNames = Arrays.stream(filesFolder.listFiles())
                .map({ it.name })
                .collect(Collectors.toList<String>())

        return fileNames;
    }

    @RequestMapping(method = arrayOf(RequestMethod.POST), value = "/upload")
    override fun fileUpload(@RequestParam("name") name: String, @RequestParam("file") file: MultipartFile): String {
        if (!file.isEmpty) {
            try {
                var stream = BufferedOutputStream(FileOutputStream(File("$filesDirectory/$name")));
                FileCopyUtils.copy(file.inputStream, stream);
                stream.close();
                return "Success"
            }
            catch (e: Exception) {
                return "Failed to upload $name => " + e.message;
            }
        }

        return "File is empty."
    }
}
