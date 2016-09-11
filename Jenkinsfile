#!/usr/bin/env groovy
@NonCPS
def feature(branchName) {
   def matcher = (branchName =~ /feature-([a-z_]+)/)
   if (matcher.matches()) {
      assert matcher.matches()
      //return matcher[0][1]
      return true
   }
   return false
}

def versionMatcher() {
   def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
   matcher ? matcher[0][1] : null
}

def pomVersion(path) {
   def pom = readMavenPom file: 'pom.xml'
   return pom.getVersion()
}

def version() {
   return pomVersion('pom.xml')
}

def branch() {
   return "${env.BRANCH_NAME}"
}

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

def findPom() {
  def baseVersion = version()

  def poms = findFiles glob: '**/pom.xml'
  for (int i = 0; i < poms.size; i++ ) {
      def files = poms[i];
      def moduleVersion = pomVersion(files.path)

      echo "name : ${files.name} path : ${files.path} directory : ${files.directory} length : ${files.length} last mod : ${files.lastModified}"
      echo "version : ${moduleVersion}"

      if (!baseVersion.equals(moduleVersion)) {
        error ('pom.xml versions inconsistent with modules')
      }
  }
}

node() {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm
   echo 'branch is: ' + branch()

   releaseCheck()
   findPom()

   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.
   def mvnHome = tool 'Maven 3.x'

   // Mark the code build 'stage'....
   stage 'Build'
   echo 'Building version ' + version()
   
   // Run the maven build
   sh "${mvnHome}/bin/mvn -B -DskipTests=true clean compile"

   stage 'Tests'
   try {
//      sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore=true verify"
      sh "${mvnHome}/bin/mvn -B verify"
   } catch (Exception e) {
     error 'test fail, please fix test and try again'
   } finally {
      stage 'Archive test results'
      step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
   }
   try {
      checkpoint 'Completed tests'
   } catch (NoSuchMethodError _) {
      echo 'Checkpoint feature available in Jenkins Enterprise'
   }
}


   if (!feature(env.BRANCH_NAME)) {
      stage concurrency: 1, name: 'Human Approval'
      input message: "Does everything really look good?"
   }

node() {
   stage 'Package'
   sh "${mvnHome}/bin/mvn -B -DskipTests=true package"
   step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war', fingerprint: true])

   // feature branches will skip this block
//   if (!isFeatureBranch(env.BRANCH_NAME)) {

      //stage 'Human Approval'
      //println("releases url: " + env.NEXUS_RELEASES_URL)
      //println("snapshot url: " + env.NEXUS_SNAPSHOT_URL)
      // don't wait forever
      //timeout(time: 24, unit: 'HOURS') {
      //  input message: "Accept publishing artifact to nexus?"
      //}
      // stage 'Publish'
      // https://www.cloudbees.com/blog/workflow-integration-credentials-binding-plugin
      // https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Binding+Plugin
      // withCredentials([
      //  [$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus-psat', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']
      // ]) {
      //  def deployCommand = "mvn deploy --batch-mode -V -s settings.xml " +
      //    " -DskipTests=true -Dmaven.javadoc.skip=true" +
      //    " -Dlocal.nexus.snapshots.password=\"" + env.PASSWORD + "\"" +
      //    " -Dlocal.nexus.snapshots.username=\"" + env.USERNAME + "\"" +
      //    " -Dlocal.nexus.releases.password=\"" + env.PASSWORD + "\"" +
      //    " -Dlocal.nexus.releases.username=\"" + env.USERNAME + "\"" +
      //    " -Dlocal.nexus.releases.url=\"" + env.NEXUS_RELEASES_URL + "\"" +
      //    " -Dlocal.nexus.snapshots.url=\"" + env.NEXUS_SNAPSHOT_URL + "\"" +
      //    " -Dlocal.nexus.mirror=\"" + env.NEXUS_MIRROR + "\""
      // sh "eval ${deployCommand}"
      //}
//   }

//   if (!feature(env.BRANCH_NAME)) {
//     stage 'Deploy'
//     sh "${mvnHome}/bin/mvn -B deploy"
//   }

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

   stage 'dockerfile'
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

//   stage 'Deploy (publish artefact)'
//   sh "${mvnHome}/bin/mvn deploy"
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
