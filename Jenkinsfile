#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _
withCredentials([usernamePassword(credentialsId: 'github_integration', passwordVariable: 'githubPassword', usernameVariable: 'githubUser'),
                 usernamePassword(credentialsId: 'github_integration_2', passwordVariable: 'githubPassword2', usernameVariable: 'githubUser2'),
                 usernamePassword(credentialsId: 'github_integration_3', passwordVariable: 'githubPassword3', usernameVariable: 'githubUser3'),
                 string(credentialsId: 'snyk-wooga-frontend-integration-token', variable: 'SNYK_TOKEN')]) {

    def testEnvironment = [
        'windows':
        [
            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser2}",
            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword2}"
        ],
        'macos':
        [
            "ATLAS_GITHUB_INTEGRATION_USER=${githubUser3}",
            "ATLAS_GITHUB_INTEGRATION_PASSWORD=${githubPassword3}"
        ]
    ]
    buildJavaLibraryOSSRH platforms: ['windows', 'macos'], testEnvironment: testEnvironment
}
