/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.plugin.mojo.test;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.plugin.mojo.ArkPluginMojo;
import mockit.Expectations;
import org.apache.maven.artifact.Artifact;
import mockit.Mocked;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class ArkPluginMojoTest {
    @Test
    public void testArkPluginMojo(@Mocked final Artifact artifact,
                                  @Mocked final MavenProject project) throws Exception {
        new Expectations() {
            {
                artifact.getGroupId();
                result = "invalid";
                minTimes = 0;
                artifact.getArtifactId();
                result = "invalid";
                minTimes = 0;
                project.getArtifact();
                result = artifact;
                minTimes = 0;
                artifact.getFile();
                result = new File(Test.class.getProtectionDomain().getCodeSource().getLocation()
                    .getPath());
            }
        };

        ArkPluginMojo arkPluginMojo = new ArkPluginMojo();
        arkPluginMojo.setProject(project);
        arkPluginMojo.setShades(new LinkedHashSet<>(Collections
            .singleton("com.alipay.sofa:test-demo:1.0.0")));
        final URL url = this.getClass().getClassLoader().getResource("test-demo.jar");

        String path = url.getPath() + ".shaded";
        String shadedUrl = url.toExternalForm() + ".shaded";
        String copyPath = url.getPath() + ".copy";
        File copyFileForTest = new File(copyPath);

        FileInputStream demoJar = new FileInputStream(new File(url.getPath()));
        FileUtils.copyInputStreamToFile(demoJar, new File(copyPath));
        demoJar.close();

        arkPluginMojo.shadeJarIntoArkPlugin(new File(path), copyFileForTest,
            Collections.singleton(artifact));

        Assert.assertTrue(copyFileForTest.delete());
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { new URL(shadedUrl) }, null);
        Assert
            .assertNotNull(urlClassLoader.loadClass("com.alipay.sofa.support.test.SampleService"));
        Assert.assertNotNull(urlClassLoader.loadClass("org.junit.Test"));
    }

    @Test
    public void testShadeJar(@Mocked final MavenProject projectOne,
                             @Mocked final MavenProject projectTwo, @Mocked final Artifact artifact) {
        ArkPluginMojo arkPluginMojo = new ArkPluginMojo();
        arkPluginMojo.setShades(new LinkedHashSet<>(Collections
            .singleton("com.alipay.sofa:test-demo:1.0.0")));

        new Expectations() {
            {
                projectOne.getGroupId();
                result = "com.alipay.sofa";
                minTimes = 0;
                projectOne.getArtifactId();
                result = "test-demo";
                minTimes = 0;

                projectTwo.getGroupId();
                result = "com.alipay.sofa";
                minTimes = 0;
                projectTwo.getArtifactId();
                result = "";
                minTimes = 0;

                artifact.getGroupId();
                result = "com.alipay.sofa";
                minTimes = 0;
                artifact.getArtifactId();
                result = "test-demo";
                artifact.getVersion();
                result = "1.0.0";
                minTimes = 0;
            }
        };

        arkPluginMojo.setProject(projectOne);
        try {
            arkPluginMojo.isShadeJar(artifact);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().equals("Can't shade jar-self."));
        }

        arkPluginMojo.setProject(projectTwo);
        Assert.assertTrue(arkPluginMojo.isShadeJar(artifact));
    }

}