package org.restler

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo

@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
class KillProcessMojo : AbstractMojo() {

    override fun execute() {
        log.info("Process is stopping.")
        CurrentProcess.killProcess()
    }

}
