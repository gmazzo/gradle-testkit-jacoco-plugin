package org.test.myplugin;

import com.squareup.javapoet.ClassName;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.test.myplugin.utils.Utils;

class MyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        Utils.INSTANCE.doSomething();
        ClassName.bestGuess("java.lang.String");
    }

}
