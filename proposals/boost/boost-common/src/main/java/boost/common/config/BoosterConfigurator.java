/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package boost.common.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.reflections.Reflections;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

public class BoosterConfigurator {

    /**
     * take a list of pom boost dependency strings and map to liberty features for
     * config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @param logger
     * @return
     * @throws BoostException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<AbstractBoosterConfig> getBoosterConfigs(List<File> jars, ClassLoader classLoader,
            Map<String, String> dependencies, BoostLoggerI logger) throws Exception {

        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(classLoader));

        List<CtClass> runtimeBoosterCtClasses = new ArrayList<CtClass>();
        for (File jar : jars) {
            List<CtClass> jarCtClasses = findBoosterCtClassesInJar(jar, classPool);
            runtimeBoosterCtClasses.addAll(jarCtClasses);
        }

        List<AbstractBoosterConfig> boosterConfigList = new ArrayList<AbstractBoosterConfig>();

        Reflections reflections = new Reflections("boost.common.boosters");

        Set<Class<? extends AbstractBoosterConfig>> allClasses = reflections.getSubTypesOf(AbstractBoosterConfig.class);
        for (Class<? extends AbstractBoosterConfig> boosterClass : allClasses) {
            if (dependencies.containsKey(AbstractBoosterConfig.getCoordinates(boosterClass))) {

                Constructor<?> cons = null;
                for (CtClass ctClass : runtimeBoosterCtClasses) {
                    if (ctClass.getSuperclass().getName().equals(boosterClass.getName())) {
                        // A runtime specific booster exists
                        Class<?> runtimeBoosterClass = classLoader.loadClass(ctClass.getName());
                        cons = runtimeBoosterClass.getConstructor(Map.class, BoostLoggerI.class);
                    }
                }

                if (cons == null) {
                    // We did not find a runtime specific booster class, just instantiate a generic
                    // one
                    cons = boosterClass.getConstructor(Map.class, BoostLoggerI.class);
                }

                Object o = cons.newInstance(dependencies, logger);
                if (o instanceof AbstractBoosterConfig) {
                    boosterConfigList.add((AbstractBoosterConfig) o);
                } else {
                    throw new BoostException(
                            "Found a booster class that did not extend AbstractBoosterConfig. This should never happen.");
                }
            }
        }

        return boosterConfigList;
    }

    public static List<String> getDependenciesToCopy(List<AbstractBoosterConfig> boosterConfigurators,
            BoostLoggerI logger) {

        Set<String> allDependencyJarsNoDups;
        List<String> dependencyJarsToCopy = new ArrayList<String>();

        for (AbstractBoosterConfig configurator : boosterConfigurators) {
            List<String> dependencyStringsToCopy = configurator.getDependencies();
            for (String depStr : dependencyStringsToCopy) {
                if (depStr != null) {
                    logger.info(depStr);
                    dependencyJarsToCopy.add(depStr);
                }
            }
        }

        allDependencyJarsNoDups = new HashSet<>(dependencyJarsToCopy);
        dependencyJarsToCopy.clear();
        dependencyJarsToCopy.addAll(allDependencyJarsNoDups);
        return dependencyJarsToCopy;
    }

    private static List<CtClass> findBoosterCtClassesInJar(File jarFile, ClassPool classPool)
            throws ClassNotFoundException, IOException {
        List<CtClass> ctClasses = new ArrayList<CtClass>();
        JarFile jar = new JarFile(jarFile);
        // Getting the files into the jar
        Enumeration<? extends JarEntry> enumeration = jar.entries();

        // Iterates into the files in the jar file
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();

            // Is this a class?
            if (zipEntry.getName().endsWith(".class")) {

                // Relative path of file into the jar.
                String className = zipEntry.getName();

                // Complete class name
                className = className.replace(".class", "").replace("/", ".");

                try {
                    CtClass ctClass = classPool.get(className);
                    // For now, assume that a runtime booster is going to directly extend a common
                    // booster, which will extend AbstractBoosterConfig
                    if (ctClass.getSuperclass().getSuperclass().getName()
                            .contains(AbstractBoosterConfig.class.getName())) {
                        ctClasses.add(ctClass);
                    }
                } catch (Exception e) {
                }

            }
        }
        jar.close();

        return ctClasses;
    }

}
