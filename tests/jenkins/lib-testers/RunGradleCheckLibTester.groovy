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


class RunGradleCheckLibTester extends LibFunctionTester {

    private String gitRepoUrl
    private String gitReference
    private String bwcCheckoutAlign

    public RunGradleCheckLibTester(gitRepoUrl, gitReference, bwcCheckoutAlign){
        this.gitRepoUrl = gitRepoUrl
        this.gitReference = gitReference
        this.bwcCheckoutAlign = bwcCheckoutAlign
    }

    @Override
    void configure(helper, binding) {
    }

    @Override
    void parameterInvariantsAssertions(call) {
        assertThat(call.args.gitRepoUrl.first(), notNullValue())
        assertThat(call.args.gitReference.first(), notNullValue())
        assertThat(call.args.bwcCheckoutAlign.first(), notNullValue())
    }

    @Override
    boolean expectedParametersMatcher(call) {
        return call.args.gitRepoUrl.first().toString().equals(this.gitRepoUrl)
                && call.args.gitReference.first().toString().equals(this.gitReference)
    }

    @Override
    String libFunctionName() {
        return 'runGradleCheck'
    }
}
