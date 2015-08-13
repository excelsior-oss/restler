package org.restler.integration

import com.fasterxml.jackson.module.paranamer.ParanamerModule
import org.restler.integration.security.SecurityConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

EnableAutoConfiguration
Import(SecurityConfig::class)
open class TestServer {

    @Bean open fun controller() = Controller()

    @Bean open fun jacksonBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder();
        builder.modules(ParanamerModule())
        return builder
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(javaClass<TestServer>(), *args)
}

