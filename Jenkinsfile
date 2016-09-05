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

def version() {
   def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
   matcher ? matcher[0][1] : null
}

node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm
   echo "branch is: ${env.BRANCH_NAME}"

   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.
   def mvnHome = tool 'Maven 3.x'

   // Mark the code build 'stage'....
   stage 'Build'
   def v = version()
   if (v) {
      echo "Building version ${v}"
   }
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

   if (!feature(env.BRANCH_NAME)) {
      stage 'Human Approval'
      input message: "Does everything really look good?"
   }

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
   def dockerfile = docker.build("javaee-example:${env.BUILD_TAG}", '.')
   dockerfile.inside() {
      sh 'find /opt/jboss/wildfly/standalone/deployments'
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
