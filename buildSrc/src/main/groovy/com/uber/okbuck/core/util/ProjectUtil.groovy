package com.uber.okbuck.core.util

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.uber.okbuck.core.model.AndroidAppTarget
import com.uber.okbuck.core.model.AndroidLibTarget
import com.uber.okbuck.core.model.JavaAppTarget
import com.uber.okbuck.core.model.JavaLibTarget
import com.uber.okbuck.core.model.ProjectType
import com.uber.okbuck.core.model.Target
import com.uber.okbuck.extension.OkBuckExtension
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin

final class ProjectUtil {

    private ProjectUtil() {
        // no instance
    }

    static ProjectType getType(Project project) {
        if (project.plugins.hasPlugin(AppPlugin)) {
            return ProjectType.ANDROID_APP
        } else if (project.plugins.hasPlugin(LibraryPlugin)) {
            return ProjectType.ANDROID_LIB
        } else if (project.plugins.hasPlugin(ApplicationPlugin.class)) {
            return ProjectType.JAVA_APP
        } else if (project.plugins.hasPlugin(JavaPlugin)) {
            return ProjectType.JAVA_LIB
        } else {
            return ProjectType.UNKNOWN
        }
    }

    static Map<String, Target> getTargets(Project project) {
        ProjectType type = getType(project)
        switch (type) {
            case ProjectType.ANDROID_APP:
                project.android.applicationVariants.collectEntries { BaseVariant variant ->
                    [variant.name, new AndroidAppTarget(project, variant.name)]
                }
                break
            case ProjectType.ANDROID_LIB:
                project.android.libraryVariants.collectEntries { BaseVariant variant ->
                    [variant.name, new AndroidLibTarget(project, variant.name)]
                }
                break
            case ProjectType.JAVA_APP:
                def targets = new HashMap<String, Target>()
                targets.put(JavaAppTarget.MAIN, new JavaAppTarget(project, JavaAppTarget.MAIN))
                return targets
                break
            case ProjectType.JAVA_LIB:
                def targets = new HashMap<String, Target>()
                targets.put(JavaLibTarget.MAIN, new JavaLibTarget(project, JavaLibTarget.MAIN))
                return targets
                break
            default:
                [:]
                break
        }
    }

    @SuppressWarnings("GrReassignedInClosureLocalVar")
    static Target getTargetForOutput(Project rootProject, File output) {
        Target result = null
        OkBuckExtension okbuck = rootProject.okbuck
        Project project = okbuck.buckProjects.find { Project project ->
            FilenameUtils.directoryContains(project.buildDir.absolutePath, output.absolutePath)
        }

        if (project != null) {
            ProjectType type = getType(project)
            switch (type) {
                case ProjectType.ANDROID_LIB:
                    def baseVariants = project.android.libraryVariants
                    baseVariants.all { BaseVariant baseVariant ->
                        def variant = baseVariant.outputs.find { BaseVariantOutput out ->
                            (out.outputFile == output)
                        }
                        if (variant != null) {
                            result = new AndroidLibTarget(project, variant.name)
                        }
                    }
                    break
                case ProjectType.JAVA_APP:
                case ProjectType.JAVA_LIB:
                    result = new JavaLibTarget(project, JavaLibTarget.MAIN)
                    break
                default:
                    result = null
            }
        }
        return result
    }

    static File getRuntimeJar() {
        try {
            final File javaBase = new File(System.getProperty("java.home")).getCanonicalFile();
            File runtimeJar = new File(javaBase, "lib/rt.jar");
            if (runtimeJar.exists()) {
                return runtimeJar;
            }
            runtimeJar = new File(javaBase, "jre/lib/rt.jar");
            return runtimeJar.exists() ? runtimeJar : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
