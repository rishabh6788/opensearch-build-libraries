/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
void call(Map args = [:]) {
    lib = library(identifier: 'jenkins@main', retriever: legacySCM(scm))

    unstash "integtest-opensearch-dashboards-${BUILD_NUMBER}"

    if (env.platform == 'windows') {
        echo "On Windows Platform, unstash repository and download the artifacts"
        echo "Downloading from S3: ${artifactPathOpenSearch}"
        downloadFromS3(
                assumedRoleName: 'opensearch-bundle',
                roleAccountNumberCred: 'jenkins-aws-account-public',
                downloadPath: "${artifactPathOpenSearch}/",
                bucketName: "${ARTIFACT_BUCKET_NAME}",
                localPath: "${WORKSPACE}/artifacts",
                force: true
        )
        sh("cp -a ${WORKSPACE}/artifacts/${artifactPathOpenSearch} ${WORKSPACE}")

        echo "Downloading from S3: ${artifactPath}"
        downloadFromS3(
                assumedRoleName: 'opensearch-bundle',
                roleAccountNumberCred: 'jenkins-aws-account-public',
                downloadPath: "${artifactPath}/",
                bucketName: "${ARTIFACT_BUCKET_NAME}",
                localPath: "${WORKSPACE}/artifacts",
                force: true
        )
        sh("cp -a ${WORKSPACE}/artifacts/${artifactPath} ${WORKSPACE}")
        sh("rm -rf ${WORKSPACE}/artifacts")
    }
    else {
        echo "Not on Windows, unstash repository+artifacts"
    }

    sh("rm -rf test-results")

    runIntegTestScript(
            jobName: "${BUILD_JOB_NAME}",
            componentName: args.localComponent,
            buildManifest: "${BUILD_MANIFEST}",
            testManifest: "${TEST_MANIFEST}",
            localPath: "${WORKSPACE}/${distribution}",
            switchUserNonRoot: args.switchUserNonRoot,
            ciGroup: args.ciGroup
    )
}
