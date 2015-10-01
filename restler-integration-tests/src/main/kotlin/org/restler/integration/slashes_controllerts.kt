package org.restler.integration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Configuration
open class SlashesConfig {

    @Bean open fun noSlashes() = NoSlashedController()
    @Bean open fun bothSlashes() = BothSlashesController()
    @Bean open fun leftSlash() = LeftSlashController()
    @Bean open fun rightSlash() = RightSlashController()

}

open class BaseSlashController {

    @RequestMapping("noSlashes")
    open fun noSlashes() = "noSlashes"

    @RequestMapping("/bothSlashes/")
    open fun bothSlashes() = "bothSlashes"

    @RequestMapping("/leftSlash")
    open fun leftSlash() = "leftSlash"

    @RequestMapping("rightSlash/")
    open fun rightSlash() = "rightSlash"

}

@RestController
@RequestMapping("noSlash")
open class NoSlashedController : BaseSlashController()

@RestController
@RequestMapping("/bothSlash/")
open class BothSlashesController : BaseSlashController()

@RestController
@RequestMapping("/leftSlash")
open class LeftSlashController : BaseSlashController()

@RestController
@RequestMapping("rightSlash/")
open class RightSlashController : BaseSlashController()