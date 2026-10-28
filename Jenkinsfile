// CI pipeline for the UNEB e-Results Splash Screen app.
// The Checkstyle stage is a quality gate: any style error fails the build
// and the later stages (build, test, archive) are skipped.
//
// Jenkins requirements:
//   Plugins: Git, Pipeline, Warnings, JUnit, Timestamper, Email Extension
//   Agent:   JDK 17+ and the Android SDK, with ANDROID_HOME set
//   Email:   SMTP configured under Manage Jenkins -> System -> Extended E-mail Notification

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    // Builds on every push via the GitHub webhook (exposed through ngrok).
    // Polling every 5 minutes is a fallback for when the tunnel is down.
    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
    }

    environment {
        NOTIFY_EMAIL = 'aarontaremwa8@gmail.com'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Checkstyle (Style Gate)') {
            steps {
                gradlew('checkstyle --no-daemon')
            }
        }

        stage('Build APK') {
            steps {
                gradlew('assembleDebug --no-daemon')
            }
        }

        stage('Unit Tests') {
            steps {
                gradlew('testDebugUnitTest --no-daemon')
            }
        }

        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
            }
        }
    }

    post {
        always {
            // Publish the Checkstyle report even when the gate fails
            recordIssues(
                enabledForFailure: true,
                tools: [checkStyle(pattern: 'app/build/reports/checkstyle/*.xml')],
                qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: false]]
            )
            junit allowEmptyResults: true, testResults: 'app/build/test-results/**/*.xml'
        }
        success {
            echo 'Style gate passed, APK built and archived.'
        }
        failure {
            echo 'Build failed. Open "CheckStyle Warnings" for the file, line and rule.'
            emailext(
                to: env.NOTIFY_EMAIL,
                subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                attachLog: true,
                body: """<p>Build <b>#${env.BUILD_NUMBER}</b> of <b>${env.JOB_NAME}</b> failed.</p>
                         <p>Most likely cause: Checkstyle style violations (style gate).</p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a><br/>
                         Checkstyle report: <a href="${env.BUILD_URL}checkstyle">${env.BUILD_URL}checkstyle</a></p>"""
            )
        }
        fixed {
            emailext(
                to: env.NOTIFY_EMAIL,
                subject: "FIXED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """<p>Build <b>#${env.BUILD_NUMBER}</b> of <b>${env.JOB_NAME}</b> is green again.</p>
                         <p>Style gate passed and the APK was archived: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>"""
            )
        }
    }
}

// Runs the Gradle wrapper on both Windows and Linux agents
def gradlew(String args) {
    if (isUnix()) {
        sh "chmod +x gradlew && ./gradlew ${args}"
    } else {
        bat "gradlew.bat ${args}"
    }
}
