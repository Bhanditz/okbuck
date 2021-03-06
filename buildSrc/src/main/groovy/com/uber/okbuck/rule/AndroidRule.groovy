package com.uber.okbuck.rule

import com.uber.okbuck.printable.PostProcessClassessCommands
import org.apache.commons.lang.StringUtils

abstract class AndroidRule extends BuckRule {

    private final Set<String> mSrcTargets
    private final Set<String> mSrcSet
    private final String mManifest
    private final String mRobolectricManifest
    private final List<String> mAnnotationProcessors
    private final List<String> mAptDeps
    private final List<String> mAidlRuleNames
    private final String mAppClass
    private final String mSourceCompatibility
    private final String mTargetCompatibility
    private final PostProcessClassessCommands mPostprocessClassesCommands
    private final List<String> mOptions
    private final List<String> mTestRunnerJvmArgs
    private final Set<String> mProvidedDeps
    private final boolean mGenerateR2
    private final String mResourcesDir
    private final String mRuntimeDependency
    private final List<String> mTestTargets
    private final List<String> mLabels

    /**
     * @srcTargets , used for SqlDelight support(or other case), genrule's output will be used as src, pass empty set if not present
     * @param appClass , if exopackage is enabled, pass the detected app class, otherwise, pass null
     * */
    AndroidRule(
            String ruleType,
            String name,
            List<String> visibility,
            List<String> deps,
            Set<String> srcTargets,
            Set<String> srcSet,
            String manifest,
            String robolectricManifest,
            List<String> annotationProcessors,
            List<String> aptDeps,
            Set<String> providedDeps,
            List<String> aidlRuleNames,
            String appClass,
            String sourceCompatibility,
            String targetCompatibility,
            PostProcessClassessCommands postprocessClassesCommands,
            List<String> options,
            List<String> testRunnerJvmArgs,
            boolean generateR2,
            String resourcesDir,
            String runtimeDependency,
            List<String> testTargets,
            List<String> labels = null) {
        super(ruleType, name, visibility, deps)

        mSrcTargets = srcTargets
        mSrcSet = srcSet
        mManifest = manifest
        mRobolectricManifest = robolectricManifest
        mAnnotationProcessors = annotationProcessors
        mAptDeps = aptDeps
        mAidlRuleNames = aidlRuleNames
        mAppClass = appClass
        mSourceCompatibility = sourceCompatibility
        mTargetCompatibility = targetCompatibility
        mPostprocessClassesCommands = postprocessClassesCommands
        mOptions = options
        mTestRunnerJvmArgs = testRunnerJvmArgs
        mProvidedDeps = providedDeps
        mGenerateR2 = generateR2
        mResourcesDir = resourcesDir
        mRuntimeDependency = runtimeDependency
        mTestTargets = testTargets
        mLabels = labels
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (mSrcTargets.empty) {
            printer.println("\tsrcs = glob([")
        } else {
            printer.println("\tsrcs = [")
            for (String target : mSrcTargets) {
                printer.println("\t\t'${target}',")
            }
            printer.println("\t] + glob([")
        }
        for (String src : mSrcSet) {
            printer.println("\t\t'${src}/**/*.java',")
        }

        if (!StringUtils.isEmpty(mAppClass)) {
            printer.println("\t], excludes = ['${mAppClass}']),")
        } else {
            printer.println("\t]),")
        }

        if (mTestTargets) {
            printer.println("\ttests = [")
            for (String testTarget : mTestTargets) {
                printer.println("\t\t'${testTarget}',")
            }
            printer.println("\t],")
        }

        if (mResourcesDir) {
            printer.println("\tresources = glob([")
            printer.println("\t\t'${mResourcesDir}/**',")
            printer.println("\t]),")

            printer.println("\tresources_root = '${mResourcesDir}',")
        }

        if (!StringUtils.isEmpty(mManifest)) {
            printer.println("\tmanifest = '${mManifest}',")
        }

        if (!StringUtils.isEmpty(mRobolectricManifest)) {
            printer.println("\trobolectric_manifest = '${mRobolectricManifest}',")
        }

        if (!mAnnotationProcessors.empty) {
            printer.println("\tannotation_processors = [")
            for (String processor : mAnnotationProcessors) {
                printer.println("\t\t'${processor}',")
            }
            printer.println("\t],")
        }

        if (!mAptDeps.empty) {
            printer.println("\tannotation_processor_deps = [")
            for (String dep : mAptDeps.sort()) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        }

        if (!mProvidedDeps.empty) {
            printer.println("\tprovided_deps = [")
            for (String dep : mProvidedDeps.sort()) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        }

        if (!mAidlRuleNames.empty) {
            printer.println("\texported_deps = [")
            mAidlRuleNames.sort().each { String aidlRuleName ->
                printer.println("\t\t'${aidlRuleName}',")
            }
            printer.println("\t],")
        }

        printer.println("\tsource = '${mSourceCompatibility}',")
        printer.println("\ttarget = '${mTargetCompatibility}',")
        mPostprocessClassesCommands.print(printer)

        if (!mOptions.empty) {
            printer.println("\textra_arguments = [")
            mOptions.each { String option ->
                printer.println("\t\t'${option}',")
            }
            printer.println("\t],")
        }

        if (mGenerateR2) {
            printer.println("\tfinal_r_name = 'R2',")
        }

        if (mRuntimeDependency) {
            printer.println("\trobolectric_runtime_dependency = '${mRuntimeDependency}',")
        }

        if (mLabels) {
            printer.println("\tlabels = [")
            mLabels.each { String label ->
                printer.println("\t\t'${label}',")
            }
            printer.println("\t],")
        }

        if (mTestRunnerJvmArgs) {
            printer.println("\tvm_args = [")
            mTestRunnerJvmArgs.each { String arg ->
                printer.println("\t\t'${arg}',")
            }
            printer.println("\t],")
        }
    }
}
