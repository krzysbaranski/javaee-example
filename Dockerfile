FROM wildfly/wildfly:10.0.0

ADD ./target/*.war /opt/jboss/wildfly/standalone/deployments/