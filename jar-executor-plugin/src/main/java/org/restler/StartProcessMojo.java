package org.restler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import java.util.function.Consumer;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartProcessMojo extends AbstractMojo  {

    @Parameter(required = true)
    private String jar;

    @Parameter(defaultValue = "0")
    private Long delay;

    @Override
    protected void finalize() throws Throwable {
        CurrentProcess.killProcess();
        super.finalize();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {


            if(!jar.endsWith(".jar")) {
                getLog().error("File must be jar.");
                throw new MojoFailureException("File must be jar.");
            }
            else if(!(new File(jar)).exists()) {
                getLog().error("Can't find file " + jar);
                throw new MojoFailureException("Can't find file " + jar);
            }

            Process newProcess = Runtime.getRuntime().exec("java -jar " + jar);
            if(newProcess.isAlive()) {
                getLog().info("Process is started.");
            }

            runInputStreamReaderThread(newProcess.getInputStream(), (String line)->getLog().info(line));
            runInputStreamReaderThread(newProcess.getErrorStream(), (String line)->getLog().error(line));

            if(delay > 0) {
                getLog().info("Start waiting " + delay + "ms ...");
                Thread.sleep(delay);
                if(newProcess.isAlive()) {
                    getLog().info("Stop waiting.");
                }
            }

            if(!newProcess.isAlive()) {
                getLog().info("Process is stopped.");
            }

            CurrentProcess.setProcess(newProcess);
        } catch (IOException | InterruptedException e) {
            getLog().error(e.getMessage());
        }
    }

    private void runInputStreamReaderThread(InputStream inputStream, Consumer<String> printMessage) {
        Thread inputThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = br.readLine();
                while (line != null) {
                    printMessage.accept(line);
                    line = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }
}
