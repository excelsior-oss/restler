package org.restler.integration.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint

Configuration
EnableWebSecurity
public open class SecurityConfig : WebSecurityConfigurerAdapter() {

    Autowired
    public fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.
                inMemoryAuthentication().
                withUser("user").password("password").roles("USER")
    }

    override fun configure(http: HttpSecurity) {
        http.
                exceptionHandling().authenticationEntryPoint(Http403ForbiddenEntryPoint()).and().
                csrf().disable().
                authorizeRequests().
                antMatchers("/secured/**").hasRole("USER").and()

        http.
                httpBasic().and().
                formLogin()
    }
}