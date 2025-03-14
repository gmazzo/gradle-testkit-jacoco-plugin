package org.test.myplugin;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyPluginTest {

    @Test
    public void myTest() throws IOException {
        File projectDir = new File(".");

        new File(projectDir, "settings.gradle").createNewFile();

        FileWriter wr = new FileWriter(new File(projectDir, "build.gradle"));
        wr.append("plugins { id 'myPlugin' }");
        wr.close();

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .build();
    }

}
