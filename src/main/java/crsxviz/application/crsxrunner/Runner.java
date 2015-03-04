package crsxviz.application.crsxrunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {

    private static String CRSX_PARSER = "crsx_parse";

    public String run(String exeName, String wrapper, String term, String dbOutPath) {
        if (exeName == null || exeName.length() == 0) {
            throw new IllegalArgumentException("Executable to run must be set");
        }
        if (term == null) {
            throw new IllegalArgumentException("Term cannot be NULL");
        }
        if (dbOutPath == null) {
            throw new IllegalArgumentException("Database output path cannot be NULL");
        }

        String cmd = exeName;
        if (wrapper != null && wrapper.length() > 1) {
            cmd += " wrapper=\"" + wrapper + "\"";
        }
        cmd += " term=\"" + term + "\"";
        cmd = cmd + " crsxviz | " + getParserPath() + " " + dbOutPath;

        try {
            System.out.println("Running `" + cmd + "`");
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader cmdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (p.isAlive()) {
                System.out.print("Waiting for processing to complete");
            }
            while (p.isAlive()) {
                System.out.print(".");
                Thread.sleep(1000);
            }
            System.out.println("\nProcessing completed");
            String out = "";
            String tmp;
            while ((tmp = cmdOut.readLine()) != null) {
                out += tmp;
            }
            cmdOut.close();
            return out;
        } catch (IOException e) {
            System.out.println("Error starting processing");
            e.printStackTrace();

        } catch (InterruptedException e) {
            System.out.println("Unexpected thread interruption");
            e.printStackTrace();
        }
        return null;
    }

    private String getParserPath() {
        return getExecResults("which " + CRSX_PARSER);
    }

    private String getExecResults(String command) {
        try {
            System.out.println("Running command `" + command + "`");
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader cmdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (p.isAlive()) {
                System.out.print("Waiting for command execution to complete");
            }
            while (p.isAlive()) {
                System.out.print(".");
                Thread.sleep(100);
            }
            System.out.println("\nCommand execution completed");
            String out = "";
            String tmp;
            while ((tmp = cmdOut.readLine()) != null) {
                out += tmp;
            }
            cmdOut.close();
            if (p.exitValue() != 0) {
                throw new RuntimeException("Error running command. Return value: " + p.exitValue());
            }
            return out;
        } catch (IOException e) {
            System.out.println("IOError while running command `" + command + "`");
            e.printStackTrace();
            throw new RuntimeException("IOError while running command");
        } catch (InterruptedException e) {
            System.out.println("Unexpected thread interruption");
            e.printStackTrace();
            throw new RuntimeException("Thread interrupted while running command");
        }
    }
}
