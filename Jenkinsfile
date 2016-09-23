#!/usr/bin/env groovy

import groovy.transform.Field

@Field
String branchName = ""

// check if branch name starts with "feature-"
@NonCPS
def isFeatureBranch() {
  def matcher = (branch() =~ /feature-([a-z_]+)/)
  if (matcher.matches()) {
    assert matcher.matches()
    //return matcher[0][1]
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
   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.
   sh 'env'
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
     sh "${mvnHome}/bin/mvn -B -DskipTests=true clean compile"
   }

   stage('Tests') {
     try {
       // sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore=true verify"
       sh "${mvnHome}/bin/mvn -B verify"
     } catch (Exception e) {
       error 'test fail, please fix test and try again'
     } finally {
       echo 'Archive test results'
       step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
     }
   }
}

node() {
  // Get the maven tool.
  // ** NOTE: This 'M3' maven tool must be configured
  // **       in the global configuration.
  def mvnHome = tool 'Maven 3.x'

  stage('Package') {
    sh "${mvnHome}/bin/mvn -B -DskipTests=true package"
    //step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war', fingerprint: true])
    step([$class: 'Fingerprinter', targets: '**/target/*.jar,**/target/*.war'])
    stash includes: '**/target/*.jar,**/target/*.war', name: 'artifacts'
  }
}

stage('Arquillian tests') {
  node() {
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
        sh "${mvnHome}/bin/mvn -B test -Parquillian-wildfly-managed"
      }
    }
  }
}

// cancel previous builds after accepting newer build
milestone label: 'milestone-to-accept', ordinal: 1
// feature branches will skip this block
if (!isFeatureBranch()) {
  // don't wait forever
  timeout(time: 24, unit: 'HOURS') {
    input message: "Accept publishing artifact to nexus from branch: " + branch()
  }
} else {
  echo "auto accepted"
}
milestone label: 'milestone-accepted', ordinal: 2

node() {
  stage('Publish') {
    milestone label: 'milestone-deploy', ordinal: 3
    def mvnHome = tool 'Maven 3.x'
    println("releases url: " + env.NEXUS_RELEASES_URL)
    println("snapshot url: " + env.NEXUS_SNAPSHOT_URL)
    // https://www.cloudbees.com/blog/workflow-integration-credentials-binding-plugin
    // https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin
    withCredentials([
      [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
    ]) {
      def deployCommand = "${mvnHome}/bin/mvn deploy --batch-mode -V -s settings.xml " +
        " -DskipTests=true -Dmaven.javadoc.skip=true" +
        " -Dlocal.nexus.snapshots.password=\"" + env.PASSWORD + "\"" +
        " -Dlocal.nexus.snapshots.username=\"" + env.USERNAME + "\"" +
        " -Dlocal.nexus.releases.password=\"" + env.PASSWORD + "\"" +
        " -Dlocal.nexus.releases.username=\"" + env.USERNAME + "\"" +
        " -Dlocal.nexus.releases.url=\"" + env.NEXUS_RELEASES_URL + "\"" +
        " -Dlocal.nexus.snapshots.url=\"" + env.NEXUS_SNAPSHOT_URL + "\"" +
        " -Dlocal.nexus.mirror=\"" + env.NEXUS_MIRROR + "\""
      sh "eval ${deployCommand}"
    }
  }
}

//   def maven = docker.image('maven:latest')
//   maven.pull() // make sure we have the latest available from Docker Hub
//   maven.inside {
//       …as above
//   }

//   stage 'docker'
//   def jboss = docker.image('krzysbaranski/wildfly:7.1.1')
//   jboss.pull()
//   jboss.inside() {
//      sh 'find /opt/jboss/wildfly/standalone/deployments'
//   }

node("docker") {
   stage('dockerfile') {
   def dockername = "javaee-example:${env.BUILD_TAG}"
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
   } finally {
     echo 'container stop'
     // add http://jenkins/scriptApproval/
     // method groovy.lang.GroovyObject getProperty java.lang.String
     container.stop()
     echo "docker rmi"
     def dockerrmi = "docker rmi " + dockername
     sh "eval ${dockerrmi}"
   }
   }

//   stage 'Deploy (publish artefact)'
//   sh "${mv}/bin/mvn deploy"
//
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
}


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
