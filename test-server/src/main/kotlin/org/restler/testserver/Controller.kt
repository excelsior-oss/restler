package org.restler.testserver

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

RestController
public open class Controller {

    RequestMapping(value = "get")
    open fun publicGet() = "OK"

    RequestMapping(value = "secured/get")
    open fun securedGet() = "Secure OK"
}
