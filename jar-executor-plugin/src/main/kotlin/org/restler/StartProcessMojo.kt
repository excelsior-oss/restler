package org.restler

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.*
import kotlin.concurrent.thread

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
class StartProcessMojo : AbstractMojo() {

    @Parameter(required = true, defaultValue = "")
    private lateinit var jar: String

    @Parameter(defaultValue = "0")
    private val delay: Long = 0

    override fun execute() {
        try {
            if (!jar.endsWith(".jar")) {
                log.error("File must be jar.")
                throw MojoFailureException("File must be jar.")
            } else if (!File(jar).exists()) {
                log.error("Can't find file " + jar)
                throw MojoFailureException("Can't find file " + jar)
            }

            val newProcess = Runtime.getRuntime().exec("java -jar $jar")
            if (newProcess.isAlive) {
                log.info("Process is started.")
            }

            runInputStreamReader(newProcess.inputStream) { log.info(it) }
            runInputStreamReader(newProcess.errorStream) { log.error(it) }

            if (delay > 0) {
                log.info("Start waiting " + delay + "ms ...")
                Thread.sleep(delay)
                if (newProcess.isAlive) {
                    log.info("Stop waiting.")
                }
            }

            if (!newProcess.isAlive) {
                log.info("Process is stopped.")
            }

            CurrentProcess.setProcess(newProcess);
        } catch (e: IOException) {
            log.error(e.message)
        } catch (e: InterruptedException) {
            log.error(e.message)
        }
    }

    private fun runInputStreamReader(inputStream: InputStream, printMessage: (String) -> Unit) {
        thread(isDaemon = true) {
            BufferedReader(InputStreamReader(inputStream)).use { br ->
                br.forEachLine(printMessage)
            }
        }
    }
}
