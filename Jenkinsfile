#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _
withCredentials([usernamePassword(credentialsId: 'github_integration', passwordVariable: 'githubPassword', usernameVariable: 'githubUser'),
                 string(credentialsId: 'spock_github_extension_coveralls_token', variable: 'coveralls_token')]) {

    def testEnvironment = [
            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser}",
            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword}"
    ]
    buildJavaLibraryOSSRH coverallsToken: coveralls_token, testEnvironment: testEnvironment
}
