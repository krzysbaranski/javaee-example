node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm

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

   stage 'Deploy (publish artefact)'
   sh "${mvnHome}/bin/mvn deploy"
}
