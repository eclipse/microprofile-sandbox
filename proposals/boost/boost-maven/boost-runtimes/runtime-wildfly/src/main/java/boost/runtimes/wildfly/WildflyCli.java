package boost.runtimes.wildfly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import boost.common.BoostLoggerI;
import net.wasdev.wlp.common.plugins.util.OSUtil;

public class WildflyCli {
	
	private final String wildflyInstallDir;
	private final String cliScript;
	private final BoostLoggerI logger;
	
	public WildflyCli(String wildflyInstallDir, BoostLoggerI logger) {
		this.wildflyInstallDir = wildflyInstallDir;
		this.logger = logger;
		
		if (OSUtil.isWindows()) {
            this.cliScript = "jboss-cli.bat";
        } else {
            this.cliScript = "jboss-cli.sh";
        }
	}
	
	public void run(String command) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(wildflyInstallDir + "/bin/" + cliScript,
                "--commands=embed-server," + command);

        logger.info("Issuing cli command: " + command);

        Process cliProcess = pb.start();

        // Print error stream
        BufferedReader error = new BufferedReader(new InputStreamReader(cliProcess.getErrorStream()));
        String line;
        while ((line = error.readLine()) != null) {
            logger.info(line);
        }

        // Print output stream
        BufferedReader in = new BufferedReader(new InputStreamReader(cliProcess.getInputStream()));
        while ((line = in.readLine()) != null) {
            logger.info(line);
        }

        // TODO: throw exception if error stream has any content

    }

}
