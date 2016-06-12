@NonCPS
def feature(branchName) {
  def matcher = (env.BRANCH_NAME =~ /feature-([a-z_]+)/)
  assert matcher.matches()
  matcher[0][1]
}
node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm
   echo "branch is: ${env.BRANCH_NAME}"
   def feature = feature(env.BRANCH_NAME)
   echo "Building flavor ${feature}"

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

   input message: "Does everything really look good?"
   stage 'Human Approval'

   stage 'Package'
   sh "${mvnHome}/bin/mvn package"

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
