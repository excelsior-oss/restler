package org.restler.testserver

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

RestController
public class Controller {

    RequestMapping(value = array("get"))
    fun publicGet() = "OK"

    RequestMapping(value = array("secured/get"))
    fun securedGet() = "Secure OK"
}
