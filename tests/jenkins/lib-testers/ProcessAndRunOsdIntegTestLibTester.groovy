/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat

class ProcessAndRunOsdIntegTestLibTester extends LibFunctionTester {

    private String localComponent
    private String ciGroup
    private String switchUserNonRoot

    public ProcessAndRunOsdIntegTestLibTester(localComponent, ciGroup, switchUserNonRoot) {
        this.localComponent = localComponent
        this.ciGroup = ciGroup
        this.switchUserNonRoot = switchUserNonRoot
    }

    @Override
    String libFunctionName() {
        return 'processAndRunOsdIntegTest'
    }

    @Override
    void parameterInvariantsAssertions(Object call) {
        assertThat(call.args.localComponent.first(), notNullValue())
        assertThat(call.args.switchUserNonRoot.first(), notNullValue())
    }

    @Override
    boolean expectedParametersMatcher(Object call) {
        return call.args.localComponent.first().toString().equals(this.localComponent)
                && call.args.switchUserNonRoot.first().toString().equals(this.switchUserNonRoot)
    }

    @Override
    void configure(Object helper, Object binding) {
        helper.registerAllowedMethod('downloadFromS3', [Map.class], { args -> return null })
        helper.registerAllowedMethod('runIntegTestScript', [Map.class], { args -> return null })
        helper.registerAllowedMethod('unstash', [String.class], { name -> return null })
        helper.registerAllowedMethod("withAWS", [Map, Closure], {
            args,
            closure ->
                closure.delegate = delegate
                return helper.callClosure(closure)
        })
        binding.setVariable('BUILD_NUMBER', '307')
        binding.setVariable('BUILD_JOB_NAME', 'dummy-integ-test')
        binding.setVariable('artifactPathOpenSearch', 'dummy-artifact-path-os')
        binding.setVariable('ARTIFACT_BUCKET_NAME', 'test-bucket')
        binding.setVariable('WORKSPACE', '/home/user')
        binding.setVariable('artifactPath', 'dummy-artifact-path')
        binding.setVariable('BUILD_MANIFEST', 'tests/data/opensearch-1.3.0-build.yml')
        binding.setVariable('TEST_MANIFEST', 'tests/data/opensearch-1.3.0-test.yml')
        binding.setVariable('distribution', 'x64')
    }
}
