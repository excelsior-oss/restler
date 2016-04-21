package org.restler

object CurrentProcess {

    private var onShutdownHook: Thread
    private var process: Process? = null

    init {
        //if maven process stopped when jar process still alive.
        onShutdownHook = Thread { CurrentProcess.killProcess() }
        Runtime.getRuntime().addShutdownHook(onShutdownHook)
    }

    fun setProcess(newProcess: Process) {
        destroyProcess()

        process = newProcess
    }

    fun killProcess() {
        destroyProcess()

        process = null
    }

    private fun destroyProcess() {
        process?.let {
            if (it.isAlive) {
                it.destroy()
            }
        }
    }
}
