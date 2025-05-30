/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
Closure call() {
    allowedFileTypes = [".tar.gz", ".zip", ".rpm", ".deb"]

    return { argsMap -> body: {

        final foundFiles

        if (isUnix()) {
            // For Unix systems, use the find command to locate files
            foundFiles = sh(script: "find ${argsMap.artifactPath} -type f", returnStdout: true).trim().split('\n')
        } else {
            // For Windows systems, use the dir command to locate files
            foundFiles = bat(script: "bash -c \"find ${argsMap.artifactPath} -type f\"", returnStdout: true).trim().split('\n')
        }

        for (file in foundFiles) {
            def acceptTypeFound = false
            for (fileType in allowedFileTypes) {
                if (file.endsWith(fileType)) {
                    final sha512
                    final basename
                    final hash
                    echo("Creating sha for ${file}")
                    if (isUnix()){
                        sha512 = sh(script: "sha512sum ${file}", returnStdout: true).split()
                        echo("sha512: ${sha512}")
                        hash = sha512[0]
                        basename = sh(script: "basename ${sha512[1]}", returnStdout: true)
                        echo("basename: ${basename}")
                    } else {
                        sha512 = bat(script: "bash -c \"sha512sum '${file}'\"", returnStdout: true).split()
                        echo("sha512: ${sha512}")
                        echo("sha512-idx0: ${sha512[0]}")
                        echo("sha512-idx1: ${sha512[1]}")
                        echo("sha512-idx2: ${sha512[2]}")
                        echo("sha512-idx3: ${sha512[3]}")
                        echo("sha512-idx3: ${sha512[4]}")
                        echo("sha512-idx3: ${sha512[5]}")
                        hash = sha512[4]
                        basename = bat(script: "bash -c \"basename '${sha512[5]}'\"", returnStdout: true)
                        echo("basename: ${basename}")
                    }
                    //sha512 is an array [shasum, filename]
                    // writing to file accroding to opensearch requirement - "512shaHash<space><space>basename"
                    writeFile file: "${file}.sha512", text: "${sha512[]}  ${basename}"
                    acceptTypeFound = true
                    break
                }
            }
            if (!acceptTypeFound) {
                if(foundFiles.length == 1){
                    echo("Not generating sha for ${file} with artifact Path ${argsMap.artifactPath}, doesn't match allowed types ${allowedFileTypes}")
                } else {
                    echo("Not generating sha for ${file} in ${argsMap.artifactPath}, doesn't match allowed types ${allowedFileTypes}")
                }
            }
        }

    }}
}