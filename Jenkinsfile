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
   // Run the maven build
   sh "${mvnHome}/bin/mvn clean install"

   stage 'Tests'
   sh "${mvnHome}/bin/mvn test"

   try {
      checkpoint 'Completed tests'
   } catch (NoSuchMethodError _) {
      echo 'Checkpoint feature available in Jenkins Enterprise'
      }

   if (!feature(env.BRANCH_NAME)) {
      input message: "Does everything really look good?"
      stage 'Human Approval'
   }

   if (!feature(env.BRANCH_NAME)) {
      stage 'Package'
      sh "${mvnHome}/bin/mvn package"
   }

   def maven = docker.image('maven:latest')
   maven.pull() // make sure we have the latest available from Docker Hub
   maven.inside {
      // …as above
   }

   step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war’, fingerprint: true])

   // stage 'Deploy (publish artefact)'
   // sh "${mvnHome}/bin/mvn deploy"

   stage 'Server deploy'
   sh "/opt/wildfly-10.0.0.Final/bin/jboss-cli.sh --controller=\"localhost:9990\" -c command=\"deploy target/AwesomeApp.war --force\""

   stage 'Test deploy'
   sh "curl  --fail -v http://localhost:8080/AwesomeApp/rest/books"

   // https://hub.docker.com/_/postgres/
   // stage 'docker postgres'
   // sh "docker run --name my-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres"
   // stage 'initdb'
   // sh "docker run -it --rm --link some-postgres:postgres postgres psql -h postgres -U postgres"
   // stage 'docker app'
   // sh "docker run --name some-app --link some-postgres:postgres -d application-that-uses-postgres"

}
