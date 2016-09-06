FROM jboss/wildfly:10.0.0.Final

ADD ./target/*.war /opt/jboss/wildfly/standalone/deployments/
