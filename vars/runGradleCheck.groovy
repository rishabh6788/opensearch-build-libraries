/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
void call(Map args = [:]) {
    def lib = library(identifier: 'jenkins@main', retriever: legacySCM(scm))
    def git_repo_url = args.gitRepoUrl ?: 'null'
    def git_reference = args.gitReference ?: 'null'
    def bwc_checkout_align = args.bwcCheckoutAlign ?: 'false'
    def bwc_checkout_align_param = ''

    println("Git Repo: ${git_repo_url}")
    println("Git Reference: ${git_reference}")
    println("Bwc Checkout Align: ${bwc_checkout_align}")

    if (Boolean.parseBoolean(bwc_checkout_align)) {
        bwc_checkout_align_param = '-Dbwc.checkout.align=true'
    }

    if (git_repo_url.equals('null') || git_reference.equals('null')) {
        println("No git repo url or git reference to checkout the commit, exit 1")
        System.exit(1)
    }
    else {
        withCredentials([
            usernamePassword(credentialsId: "jenkins-gradle-check-s3-aws-credentials", usernameVariable: 'amazon_s3_access_key', passwordVariable: 'amazon_s3_secret_key'),
            usernamePassword(credentialsId: "jenkins-gradle-check-s3-aws-resources", usernameVariable: 'amazon_s3_base_path', passwordVariable: 'amazon_s3_bucket')]) {

            sh """
                #!/bin/bash

                set -e
                set +x

                echo "Git clone: ${git_repo_url} with ref: ${git_reference}"
                rm -rf search
                git clone ${git_repo_url} search
                cd search/
                git checkout -f ${git_reference}
                git rev-parse HEAD

                echo "Get Major Version"
                OS_VERSION=`cat gradle/libs.versions.toml | grep opensearch | cut -d= -f2 | grep -oE '[0-9.]+'`
                JDK_MAJOR_VERSION=`cat gradle/libs.versions.toml | grep "bundled_jdk" | cut -d= -f2 | grep -oE '[0-9]+'  | head -n 1`
                OS_MAJOR_VERSION=`echo \$OS_VERSION | grep -oE '[0-9]+' | head -n 1`
                echo "Version: \$OS_VERSION, Major Version: \$OS_MAJOR_VERSION"

                echo "Using JAVA \$JDK_MAJOR_VERSION"
                eval export JAVA_HOME='\$JAVA'\$JDK_MAJOR_VERSION'_HOME'

                env | grep JAVA | grep HOME

                echo "Gradle clean cache and stop existing gradledaemon"
                ./gradlew --stop
                rm -rf ~/.gradle

                if command -v docker > /dev/null; then
                    echo "Check existing dockercontainer"
                    docker ps -a
                    docker stop `docker ps -qa` > /dev/null 2>&1 || echo
                    docker rm --force `docker ps -qa` > /dev/null 2>&1 || echo
                    echo "Stop existing dockercontainer"
                    docker ps -a

                    echo "Check docker-compose version"
                    docker-compose version
                fi

                echo "Check existing processes"
                ps -ef | grep [o]pensearch | wc -l
                echo "Cleanup existing processes"
                kill -9 `ps -ef | grep [o]pensearch | awk '{print \$2}'` > /dev/null 2>&1 || echo
                ps -ef | grep [o]pensearch | wc -l

                echo "Start gradlecheck"
                GRADLE_CHECK_STATUS=0
                ./gradlew clean && ./gradlew check -Dtests.coverage=true ${bwc_checkout_align_param} --no-daemon --no-scan || GRADLE_CHECK_STATUS=1

                if [ "\$GRADLE_CHECK_STATUS" != 0 ]; then
                    echo Gradle Check Failed!
                    exit 1
                fi

            """
        }

    }


}
