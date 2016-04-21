package org.restler

object CurrentProcess {
    private var onShutdownHook: Thread
    private var process: Process? = null

    init {
        //if maven process stopped when jar process still alive.
        onShutdownHook = Thread({CurrentProcess.killProcess()})
        Runtime.getRuntime().addShutdownHook(onShutdownHook)
    }

    fun setProcess(newProcess: Process) {
        if (process != null && process!!.isAlive) {
            process!!.destroy()
        }

        process = newProcess
    }

    fun killProcess() {
        if (process != null && process!!.isAlive) {
            process!!.destroy()
        }

        process = null
    }
}
