package io.openliberty.boost.common.boosters;

import static io.openliberty.boost.common.config.ConfigConstants.*;
import static io.openliberty.boost.common.utils.DOMUtils.getDirectChildrenByTag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Element;

import boost.runtimes.LibertyServerConfigGenerator;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.config.BoostProperties;
import io.openliberty.boost.common.config.BoosterConfigurator;
import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.utils.BoosterUtil;
import io.openliberty.boost.common.utils.CommonLogger;
import io.openliberty.boost.common.utils.ConfigFileUtils;

public class JSONPBoosterTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    BoostLoggerI logger = CommonLogger.getInstance();

    /**
     * Test that the jsonp-1.1 feature is added to server.xml when the jsonp booster
     * version is set to 0.2-SNAPSHOT
     * 
     */
    @Test
    public void testJSONPBoosterFeature() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        List<AbstractBoosterConfig> boosters = BoosterConfigurator.getBoosterConfigs(
                BoosterUtil.createDependenciesWithBoosterAndVersion(JSONPBoosterConfig.class, "0.2-SNAPSHOT"), logger);

        serverConfig.addFeature(boosters.get(0).getLibertyFeature());
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";
        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML, "<feature>" + JSONP_11 + "</feature>");

        assertTrue("The " + JSONP_11 + " feature was not found in the server configuration", featureFound);

    }

}
