package org.restler.integration

import com.fasterxml.jackson.module.paranamer.ParanamerModule
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.session.HashSessionManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.restler.integration.security.SecurityConfig
import org.restler.integration.springdata.SpringDataRestConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy
import org.springframework.web.multipart.support.StandardServletMultipartResolver
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.util.EnumSet
import javax.servlet.DispatcherType
import javax.servlet.MultipartConfigElement

@EnableWebMvc
@Import(SecurityConfig::class, SpringDataRestConfig::class, SlashesConfig::class)
open class WebConfig : WebMvcConfigurerAdapter() {

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val paranamerModule = ParanamerModule()
        converters.filterIsInstance(MappingJackson2HttpMessageConverter::class.java).forEach {
            it.objectMapper.registerModule(paranamerModule)
        }
    }

    @Bean open fun multipartResolver() = StandardServletMultipartResolver()

    @Bean open fun controller() = Controller()
}

fun main(args: Array<String>) {
    val server = server()

    server.start()
    server.join()
}

fun server(): Server {
    val applicationContext = AnnotationConfigWebApplicationContext()
    applicationContext.register(WebConfig::class.java)

    val servletHolder = ServletHolder(DispatcherServlet(applicationContext))

    servletHolder.registration.setMultipartConfig(MultipartConfigElement("data/tmp"));

    val context = ServletContextHandler()
    context.sessionHandler = SessionHandler(HashSessionManager())
    context.contextPath = "/"
    context.addServlet(servletHolder, "/*")
    context.addFilter(FilterHolder(DelegatingFilterProxy("springSecurityFilterChain")), "/*", EnumSet.allOf(DispatcherType::class.java))
    context.addEventListener(ContextLoaderListener(applicationContext))

    val webPort = System.getenv("PORT") ?: "8080"

    val server = Server(Integer.valueOf(webPort))

    server.handler = context
    return server
}

