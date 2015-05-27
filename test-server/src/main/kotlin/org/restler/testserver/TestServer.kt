package org.restler.testserver

import org.restler.testserver.security.SecurityConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

EnableAutoConfiguration
Configuration
Import(javaClass<SecurityConfig>())
open class TestServer {

    Bean open fun controller() = Controller()

}

fun main(args: Array<String>) {
    SpringApplication.run(javaClass<TestServer>(), *args)
}

