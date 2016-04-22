package org.restler

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import kotlin.concurrent.thread

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
class StartProcessMojo : AbstractMojo() {

    @Parameter(required = true, defaultValue = "")
    private lateinit var jar: String

    @Parameter(defaultValue = "")
    private var waitingLine: String = ""

    @Parameter(defaultValue = "0")
    private val delay: Long = 0

    private var waitingFutureLine: CompletableFuture<Void> = CompletableFuture()
    private lateinit var waitingLinePattern: Pattern

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

            CurrentProcess.setProcess(newProcess);

            waitingLinePattern = Pattern.compile(waitingLine)

            runInputStreamReader(newProcess.inputStream) { checkWaitingLine(it); log.info(it) }
            runInputStreamReader(newProcess.errorStream) { log.error(it) }

            if (delay > 0) {
                log.info("Start waiting " + delay + "ms ...")
                Thread.sleep(delay)
                if (newProcess.isAlive) {
                    log.info("Stop waiting.")
                }
            }

            if(!waitingLine.isEmpty()) {
                waitingFutureLine.get()
            }

            if (!newProcess.isAlive) {
                log.info("Process is stopped.")
            }
        } catch (e: IOException) {
            log.error(e.message)
        } catch (e: InterruptedException) {
            log.error(e.message)
        }
    }

    private fun checkWaitingLine(line: String) {
        if(waitingLinePattern.matcher(line).matches()) {
            waitingFutureLine.complete(null)
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
