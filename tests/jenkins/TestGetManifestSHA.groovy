/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins.tests

import org.junit.Before
import org.junit.Test

class TestGetManifestSHA extends BuildPipelineTest {
    @Override
    @Before
    void setUp() {
        super.setUp()

        binding.setVariable('JOB_NAME', 'get-manifest-sha-build')
        binding.setVariable('AWS_ACCOUNT_PUBLIC', 'account')
        binding.setVariable('ARTIFACT_BUCKET_NAME', 'artifact-bucket')

        helper.registerAllowedMethod("sha1", [String], { filename ->
            return 'sha1'
        })

        helper.registerAllowedMethod("withAWS", [Map, Closure], { args, closure ->
            closure.delegate = delegate
            return helper.callClosure(closure)
        })

        helper.registerAllowedMethod("git", [Map])
        helper.registerAllowedMethod('isUnix', [], { true })
    }

    @Test
    public void testExists() {
        helper.registerAllowedMethod("s3DoesObjectExist", [Map], { args ->
            return true
        })

        super.testPipeline(
            "tests/jenkins/jobs/GetManifestSHA_Jenkinsfile",
            "tests/jenkins/jobs/GetManifestSHA_Jenkinsfile_exists"
        )
    }

    @Test
    public void testDoesNotExist() {
        helper.registerAllowedMethod("s3DoesObjectExist", [Map], { args ->
            return false
        })

        super.testPipeline(
            "tests/jenkins/jobs/GetManifestSHA_Jenkinsfile",
            "tests/jenkins/jobs/GetManifestSHA_Jenkinsfile_does_not_exist"
        )
    }
}
