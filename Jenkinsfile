// Backend ALERTA MUJER: pruebas de los 3 microservicios (auth, alert, evidence), una imagen Docker
// por servicio y, en main, despliegue en Render con el "deploy hook" de cada uno
// (credenciales render-deploy-hook-auth, -alert y -evidence en Jenkins).
def SERVICIOS = ['auth', 'alert', 'evidence']

pipeline {
  agent any
  options {
    timestamps()
    timeout(time: 40, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '15'))
    disableConcurrentBuilds()
  }

  stages {
    stage('Pruebas') {
      steps {
        // El pom padre compila shared-kernel y corre las pruebas de los tres servicios
        dir('microservicios') {
          sh 'mvn -B -ntp test'
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'microservicios/*/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Imágenes Docker') {
      steps {
        dir('microservicios') {
          script {
            // Una etiqueta por rama (feature/x → feature-x): no se acumula una imagen por build
            def tag = env.BRANCH_NAME.replaceAll('[^A-Za-z0-9_.-]', '-').toLowerCase()
            // El contexto es microservicios/: cada Dockerfile necesita el pom padre y shared-kernel
            SERVICIOS.each { s ->
              sh "docker build -f ${s}-service/Dockerfile -t alerta-mujer-${s}:${tag} ."
            }
          }
        }
      }
    }

    stage('Desplegar en Render') {
      when { branch 'main' }
      steps {
        script {
          SERVICIOS.each { s ->
            try {
              withCredentials([string(credentialsId: "render-deploy-hook-${s}", variable: 'HOOK')]) {
                sh 'curl -fsS -X POST "$HOOK" > /dev/null'
              }
              echo "Despliegue de ${s} solicitado."
            } catch (org.jenkinsci.plugins.credentialsbinding.impl.CredentialNotFoundException e) {
              echo "No hay credencial render-deploy-hook-${s}: se omite su despliegue."
            }
          }
        }
      }
    }
  }

  post {
    always { cleanWs() }
  }
}
