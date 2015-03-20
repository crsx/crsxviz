package crsxviz.application.crsxrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class Runner {

    private static String CRSX_PARSER = "crsx_parse";

	/** Pipes the contents of one stream into another then closes both streams
	 * @param is The input to copy
	 * @param os The target for the data to be copied to
	 * @throws IOException There was an error performing the IO
	 */
	static public long pipeStreams(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[512]; // 512 appears to be best performance memory allocation size on windows 64-bit HotSpot 8
		long totalLen = 0;
		int len;
		while( (len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
			totalLen += len;
		}
		os.close();
		is.close();
		return totalLen;
	}
    
    public String run(String exeName, String wrapper, String term, String dbOutPath) throws IOException {
    	String parserPath;
    	try {
    		parserPath = getParserPath();
    	} catch (RuntimeException e) {
    		InputStream parser_exe_internal = this.getClass().getClassLoader().getResourceAsStream(CRSX_PARSER);
    		if (parser_exe_internal == null) {
    			throw new IOException("Could not locate parser binary");
    		}
    		File f = new File(CRSX_PARSER);
    		if (!f.createNewFile()){
    			throw new IOException("Error extracting parser binary. Could not create file.");
    		}
    		OutputStream os = new FileOutputStream(f);
    		try {
    			pipeStreams(parser_exe_internal, os);
    		} catch (IOException ex) {
    			throw new IOException("Error extracting parser binary. Could not write file");
    		}
    		try {
    			f.setExecutable(true);
    		} catch (SecurityException ex) {
    			throw new IOException("Error setting extracted parser binary to executable.");
    		}
    		parserPath = f.getAbsolutePath();
    	}
    	
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
        cmd = cmd + " crsxviz | " + parserPath + " " + dbOutPath;

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
    	File f = new File(CRSX_PARSER);
    	if (f.exists() && f.canExecute())
    		return f.getAbsolutePath();
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
