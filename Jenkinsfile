// Backend ALERTA MUJER: pruebas (incluye la verificación del monolito modular), imagen Docker y,
// en main, despliegue en Render con su "deploy hook" (credencial render-deploy-hook en Jenkins).
pipeline {
  agent any
  options {
    timestamps()
    timeout(time: 30, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '15'))
    disableConcurrentBuilds()
  }

  stages {
    stage('Pruebas') {
      steps {
        dir('backend-monolith') {
          sh 'mvn -B -ntp test'
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'backend-monolith/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Imagen Docker') {
      steps {
        dir('backend-monolith') {
          script {
            // Una etiqueta por rama (feature/x → feature-x): no se acumula una imagen por build
            def tag = env.BRANCH_NAME.replaceAll('[^A-Za-z0-9_.-]', '-').toLowerCase()
            sh "docker build -t alerta-mujer-backend:${tag} ."
          }
        }
      }
    }

    stage('Desplegar en Render') {
      when { branch 'main' }
      steps {
        script {
          try {
            withCredentials([string(credentialsId: 'render-deploy-hook', variable: 'HOOK')]) {
              sh 'curl -fsS -X POST "$HOOK" > /dev/null'
            }
            echo 'Despliegue en Render solicitado.'
          } catch (org.jenkinsci.plugins.credentialsbinding.impl.CredentialNotFoundException e) {
            echo 'No hay credencial render-deploy-hook: se omite el despliegue.'
          }
        }
      }
    }
  }

  post {
    always { cleanWs() }
  }
}
