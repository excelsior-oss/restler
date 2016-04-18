package org.restler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
            Process newProcess = Runtime.getRuntime().exec("java -jar " + jar);
            if(newProcess.isAlive()) {
                getLog().info("Process is started.");
            }
            Thread thread = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(newProcess.getInputStream()))) {
                    String line = br.readLine();
                    while (line != null) {
                        System.out.println(line);
                        line = br.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();

            if(delay > 0) {
                getLog().info("Start waiting " + delay + "ms ...");
                Thread.sleep(delay);
                getLog().info("Stop waiting.");
            }

            CurrentProcess.setProcess(newProcess);
        } catch (IOException | InterruptedException e) {
            getLog().error(e.getMessage());
        }
    }
}
