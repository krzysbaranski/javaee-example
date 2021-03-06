#!/usr/bin/env groovy

// jenkins configuration in the global configuration:
// tool: 'Maven 3.x'
// credentials: nexus, sonarqube

import groovy.transform.Field

@Field
String branchName = ""

def boolean allowPublish() {
  if (branch().toLowerCase().startsWith('feature')) {
    return false
  }
  if (branch() == 'develop' || branch() == 'master') {
    return true
  }
  return false
}


@NonCPS
def versionMatcher() {
   def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
   matcher ? matcher[0][1] : null
}

def pomVersion(path) {
   def pom = readMavenPom file: path
   return pom.getVersion()
}

def pomPackaging(path) {
  def pom = readMavenPom file: path
  return pom.getPackaging();
}

def finalName() {
  def pom = readMavenPom file: 'pom.xml'
  return pom.getBuild().getFinalName();
}

def dockerImageName() {
  return finalName().toLowerCase()
}

def version() {
   return pomVersion('pom.xml')
}

def branch() {
   def boolean check = branchName?.trim()
   if (check) {
     // return from field
     return branchName
   }
   // read from env (env is not set outside of node)
   branchName = "${env.BRANCH_NAME}"
   check = branchName?.trim()
   if (!check) {
     error ("invalid context for branch(), first use must be in node")
   }
   return branchName
}

// check if build is from master branch and app version is correctly set
def releaseCheck() {
  def branch = branch()
  def isMaster = branch.toLowerCase().equals('master')
  def v = version()
  def isSnapshot = v.toLowerCase().contains('snapshot')

  if (isMaster && isSnapshot) {
    error ('branch ' + branch + ' should build only release version but this is ' + v)
  }
  if (!isMaster && !isSnapshot) {
    error ('branch ' + branch + ' can only build snapshot version! version is ' + v)
  }
}

// find all pom.xml files
def findPom() {
  def baseVersion = version()

  def poms = findFiles glob: '**/pom.xml'
  for (int i = 0; i < poms.length; i++ ) {
      def files = poms[i];
      def moduleVersion = pomVersion(files.path)

      echo "module ${files.path} version is ${moduleVersion}"

      if (!baseVersion.equals(moduleVersion)) {
        error ("main pom.xml version is {$baseVersion} and is inconsistent with module ${files.path} version ${moduleVersion}")
      }
  }
}

node() {
  def mvnHome = tool 'Maven 3.x'

  // Mark the code checkout 'stage'....
  stage('Checkout') {
    // Checkout code from repository
    checkout scm
    echo 'branch is: ' + branch()
  }

  // Mark the code build 'stage'....
  stage('Build') {
    releaseCheck()
    findPom()
    echo 'Building version ' + version()

    // Run the maven build
    withCredentials([
      [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
    ]) {
      sh "${mvnHome}/bin/mvn --batch-mode --update-snapshots -DskipTests=true clean compile -s settings.xml -Dlocal.nexus.mirror=\"${env.NEXUS_MIRROR}\" -Dlocal.nexus.mirror.password=\"${env.PASSWORD}\" -Dlocal.nexus.mirror.username=\"${env.USERNAME}\""
    }
  }

  stage('Package') {
    withCredentials([
      [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
    ]) {
      sh "${mvnHome}/bin/mvn package --batch-mode -DskipTests=true  -s settings.xml -Dlocal.nexus.mirror=\"${env.NEXUS_MIRROR}\" -Dlocal.nexus.mirror.password=\"${env.PASSWORD}\" -Dlocal.nexus.mirror.username=\"${env.USERNAME}\""
    }
  }
  stash 'all-files-package'
}

node() {
  stage('Tests') {
    def mvnHome = tool 'Maven 3.x'
    unstash 'all-files-package'
    withCredentials([
      [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
    ]) {
      sh "${mvnHome}/bin/mvn verify --batch-mode --update-snapshots -Dmaven.test.failure.ignore -s settings.xml -Dlocal.nexus.mirror=\"${env.NEXUS_MIRROR}\" -Dlocal.nexus.mirror.password=\"${env.PASSWORD}\" -Dlocal.nexus.mirror.username=\"${env.USERNAME}\""
    }
    junit '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
  }
}

node() {
  stage('Arquillian tests') {
    unstash 'all-files-package'
    def mvnHome = tool 'Maven 3.x'
    // turn off color in wildfly logs
    // jboss-cli /subsystem=logging/console-handler=CONSOLE:write-attribute(name=named-formatter, value=PATTERN)
    // sed -i -e 's/<named-formatter name="PATTERN"\/>/<named-formatter name="COLOR-PATTERN"\/>/g' target/wildfly-10.1.0.Final/standalone/configuration/standalone.xml
    // sed -i -e 's/handler.CONSOLE.formatter=COLOR-PATTERN/handler.CONSOLE.formatter=PATTERN/g' target/wildfly-10.1.0.Final/standalone/configuration/logging.properties
    // sed -i -e 's/formatters=COLOR-PATTERN//g' target/wildfly-10.1.0.Final/logging.properties

    // TODO random ports
    // TODO run inside docker

    // lock tcp port on current node
    def resourceLockName = "${env.NODE_NAME}:tcp-port-8080"
    lock(resource: resourceLockName, inversePrecedence: true) {
      withEnv(['JBOSS_HOME=target/wildfly-10.1.0.Final']) {
        withCredentials([
          [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
        ]) {
          wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
            sh "${mvnHome}/bin/mvn test -Parquillian-wildfly-managed -Dmaven.test.failure.ignore --batch-mode -s settings.xml -Dlocal.nexus.mirror=\"${env.NEXUS_MIRROR}\" -Dlocal.nexus.mirror.password=\"${env.PASSWORD}\" -Dlocal.nexus.mirror.username=\"${env.USERNAME}\""
          }
        }
        junit '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
      }
    }
    stash 'all-files-arquillian'
  }
}

node() {
  stage('Sonar') {
    def mvnHome = tool 'Maven 3.x'
    unstash 'all-files-package'
    withCredentials([
      [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
    ]) {
      withCredentials([
        [$class: 'UsernamePasswordMultiBinding', credentialsId: 'sonarqube', usernameVariable: 'SONAR_USERNAME', passwordVariable: 'SONAR_PASSWORD']
      ]) {
        sh "${mvnHome}/bin/mvn sonar:sonar --batch-mode -Dmaven.test.failure.ignore -s settings.xml -Dlocal.nexus.mirror=\"${env.NEXUS_MIRROR}\" -Dlocal.sonar.url=\"${env.SONAR_URL}\" -Dlocal.nexus.mirror.username=\"${env.USERNAME}\" -Dlocal.nexus.mirror.password=\"${env.PASSWORD}\" -Dsonar.branch=" + branch() + " -Dsonar.login=\"${env.SONAR_USERNAME}\" -Dsonar.password=\"${env.SONAR_PASSWORD}\""
      }
    }
  }
}

// cancel previous builds after accepting newer build
milestone label: 'milestone-to-accept', ordinal: 1

if (allowPublish()) {
  stage('Approve') {
    if (branch() == 'master') {
      // don't wait forever
      timeout(time: 72, unit: 'HOURS') {
        input message: "Accept publishing artifact to nexus from branch: " + branch()
      }
    } else {
      echo "Auto-accepted: publishing artifact to nexus from branch: " + branch()
    }
    milestone label: 'milestone-accepted', ordinal: 2
  }
}

node() {
  if (allowPublish()) {
    stage('Publish') {
      unstash 'all-files-package'
      milestone label: 'milestone-deploy', ordinal: 3
      def mvnHome = tool 'Maven 3.x'
      def packaging = pomPackaging('pom.xml')
      def deployFormatTask = ""
      // mvn require classes folder for some reason
      sh 'mkdir -p target/classes'
      if (packaging != 'pom') {
        deployFormatTask = packaging + ":" + packaging
      }
      println("releases url: " + env.NEXUS_RELEASES_URL)
      println("snapshot url: " + env.NEXUS_SNAPSHOT_URL)
      // https://www.cloudbees.com/blog/workflow-integration-credentials-binding-plugin
      // https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin
      withCredentials([
        [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
      ]) {
        sh "${mvnHome}/bin/mvn ${deployFormatTask} deploy:deploy --batch-mode -V -s settings.xml -DskipTests=true -Dmaven.javadoc.skip=true -Dlocal.nexus.snapshots.password=\"${env.PASSWORD}\" -Dlocal.nexus.snapshots.username=\"${env.USERNAME}\" -Dlocal.nexus.releases.password=\"${env.PASSWORD}\" -Dlocal.nexus.releases.username=\"${env.USERNAME}\" -Dlocal.nexus.releases.url=\"${env.NEXUS_RELEASES_URL}\" -Dlocal.nexus.snapshots.url=\"${env.NEXUS_SNAPSHOT_URL}\" -Dlocal.nexus.mirror=\"${env.NEXUS_MIRROR}\" -Dlocal.nexus.mirror.password=\"${env.PASSWORD}\" -Dlocal.nexus.mirror.username=\"${env.USERNAME}\""
      }
      //step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war', fingerprint: true])
      step([$class: 'Fingerprinter', targets: '**/target/*.jar,**/target/*.war'])
      stash includes: '**/target/*.jar,**/target/*.war', name: 'artifacts'
    }
  }
}

//   def maven = docker.image('maven:latest')
//   maven.pull() // make sure we have the latest available from Docker Hub
//   maven.inside {
//   }

//   stage 'docker'
//   def jboss = docker.image('krzysbaranski/wildfly:7.1.1')
//   jboss.pull()
//   jboss.inside() {
//      sh 'find /opt/jboss/wildfly/standalone/deployments'
//   }

node('docker') {
  if (allowPublish()) {
    stage('dockerfile') {
      unstash 'artifacts'
      def dockername = dockerImageName() + ":" + branch() + ".build-" + "${env.BUILD_ID}"
      def dockerfile = docker.build(dockername, '.')

      def container
      try {
        container = dockerfile.run()
        echo "containerId ${container.id}"
        //sh 'docker logs ${container.id}|grep "org.jboss.as.server.*Deployed.*war"'
        echo 'logs'
        containerid = container.id
        def dockerlogs = "docker logs " + containerid
        sh "eval ${dockerlogs}"
        //def cli = "docker tag " + dockername  + "localhost:5000/" + dockername
        //sh "eval ${cli}"
        //def push = "docker push " + "localhost:5000/" + dockername
        //sh "eval ${push}"

        println(env.DOCKER_REGISTRY_URL)
        docker.withRegistry(env.DOCKER_REGISTRY_URL, 'docker-login') {
          dockerfile.push()
          dockerfile.push(branch())
        }
      } finally {
        // add http://jenkins/scriptApproval/
        // method groovy.lang.GroovyObject getProperty java.lang.String
        container.stop()
        //echo "docker rmi"
        //def dockerrmi = "docker rmi " + dockername
        //sh "eval ${dockerrmi}"
      }
    }
  }
}

//   stage 'Server deploy'
//   sh "/opt/wildfly-10.0.0.Final/bin/jboss-cli.sh --controller=\"localhost:9990\" -c command=\"deploy target/AwesomeApp.war --force\""
//
//   stage 'Test deploy'
//   sh "curl  --fail -v http://localhost:8080/AwesomeApp/rest/books"

//   https://hub.docker.com/_/postgres/
//   stage 'docker postgres'
//   sh "docker run --name my-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres"
//   stage 'initdb'
//   sh "docker run -it --rm --link some-postgres:postgres postgres psql -h postgres -U postgres"
//   stage 'docker app'
//   sh "docker run --name some-app --link some-postgres:postgres -d application-that-uses-postgres"



//  try {
//    ...
//
//    echo "project build successful"
//    mail body: 'project build successful',
//    from: 'xxxx@yyyyy.com',
//    replyTo: 'xxxx@yyyy.com',
//    subject: 'project build successful',
//    to: 'yyyyy@yyyy.com'
//  } catch (err) {
//    currentBuild.result = "FAILURE"
//    echo "project build error: ${err}"
//    mail body: "project build error: ${err}" ,
//    from: 'xxxx@yyyy.com',
//    replyTo: 'yyyy@yyyy.com',
//    subject: 'project build failed',
//    to: 'zzzz@yyyyy.com'
//      throw err
//  }
