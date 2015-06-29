package org.restler.integration

import org.restler.integration.security.SecurityConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

EnableAutoConfiguration
Configuration
Import(SecurityConfig::class)
open class TestServer {

    Bean open fun controller() = Controller()

}

fun main(args: Array<String>) {
    SpringApplication.run(javaClass<TestServer>(), *args)
}

