package org.restler.integration.springdata

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration
import org.springframework.http.MediaType

@Configuration
@EnableJpaRepositories(basePackages = arrayOf("org.restler.integration.springdata"))
@ComponentScan("org.restler.integration.springdata")
@Import(DbConfig::class)
open class SpringDataRestConfig : RepositoryRestMvcConfiguration() {

    override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration) {
        config.setDefaultMediaType(MediaType.APPLICATION_JSON)
    }
}
