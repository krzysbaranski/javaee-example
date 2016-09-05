FROM jboss/wildfly:10.1.0.Final

ADD ./target/*.war /opt/jboss/wildfly/standalone/deployments/
