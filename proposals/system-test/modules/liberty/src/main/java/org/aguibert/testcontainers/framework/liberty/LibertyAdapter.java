/*
 * Copyright (c) 2019 IBM Corporation and others
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aguibert.testcontainers.framework.liberty;

import java.io.File;

import org.aguibert.testcontainers.framework.spi.ServerAdapter;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class LibertyAdapter implements ServerAdapter {

    @Override
    public int getDefaultHttpPort() {
        return 9080;
    }

    @Override
    public int getDefaultHttpsPort() {
        return 9443;
    }

    @Override
    public int getDefaultAppStartTimeout() {
        return 15;
    }

    @Override
    public ImageFromDockerfile getDefaultImage(File appFile) {
        String appName = appFile.getName();
        // Compose a docker image equivalent to doing:
        // FROM open-liberty:microProfile2
        // ADD build/libs/myservice.war /config/dropins
        // COPY src/main/liberty/config /config/
        ImageFromDockerfile image = new ImageFromDockerfile()
                        .withDockerfileFromBuilder(builder -> builder.from("open-liberty:microProfile2")
                                        .add("/config/dropins/" + appName, "/config/dropins/" + appName)
                                        .copy("/config", "/config")
                                        .build())
                        .withFileFromFile("/config/dropins/" + appName, appFile)
                        .withFileFromFile("/config", new File("src/main/liberty/config"));
        return image;
    }
}
