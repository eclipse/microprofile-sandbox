package boost.common.config;

import java.util.Map;
import java.util.Properties;

/**
 * This interface contains a set of common methods for any supported runtime.
 * These methods can be called to configure the runtime with various components
 * such as HTTP endpoints, datasources, etc.
 *
 */
public interface ServerConfigGenerator {

    /**
     * Add the necessary server configuration to run this application
     * 
     */
    public void addApplication(String appName);

    /**
     * Configure the server with the specified keystore configuration
     * 
     */
    public void addKeystore(Map<String, String> keystoreProps, Map<String, String> keyProps);

    /**
     * Configure the server with the specified hostname
     * 
     */
    public void addHostname(String hostname) throws Exception;

    /**
     * Configure the server with the specified HTTP port
     *
     */
    public void addHttpPort(String httpPort) throws Exception;

    /**
     * Configure the server with the specified HTTPS port
     * 
     */
    public void addHttpsPort(String httpsPort) throws Exception;

    /**
     * Configure the server with the specified datasource properties
     * 
     */
    public void addDataSource(String productName, Properties serverProperties) throws Exception;
}
