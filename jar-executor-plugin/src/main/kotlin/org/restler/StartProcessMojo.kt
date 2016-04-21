package org.restler

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import java.util.function.Consumer
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

import java.io.*

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
class StartProcessMojo : AbstractMojo() {

    @Parameter(required = true, defaultValue = "")
    private val jar: String? = null
    @Parameter(defaultValue = "0")
    private val delay: Long? = null

    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        try {
            if (!jar!!.endsWith(".jar")) {
                log.error("File must be jar.")
                throw MojoFailureException("File must be jar.")
            } else if (!File(jar).exists()) {
                log.error("Can't find file " + jar)
                throw MojoFailureException("Can't find file " + jar)
            }

            val newProcess = Runtime.getRuntime().exec("java -jar " + jar)
            if (newProcess.isAlive) {
                log.info("Process is started.")
            }

            runInputStreamReaderThread(newProcess.inputStream, { log.info(it) })
            runInputStreamReaderThread(newProcess.errorStream, { log.error(it) })

            if (delay!! > 0) {
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

    private fun runInputStreamReaderThread(inputStream: InputStream, printMessage: (String)->Unit) {
        val inputThread = Thread {
            try {
                BufferedReader(InputStreamReader(inputStream)).use { br ->
                    var line: String? = br.readLine()
                    while (line != null) {
                        printMessage(line)
                        line = br.readLine()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        inputThread.isDaemon = true
        inputThread.start()
    }
}
