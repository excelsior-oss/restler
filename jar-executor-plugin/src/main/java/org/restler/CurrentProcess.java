package org.restler;

public class CurrentProcess {
    private static Process process = null;

    public static void setProcess(Process newProcess) {
        if(process != null && process.isAlive()) {
            process.destroy();
        }

        process = newProcess;
    }

    public static void killProcess() {
        if(process != null && process.isAlive()) {
            process.destroy();
        }

        process = null;
    }
}
