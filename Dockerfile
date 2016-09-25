FROM jboss/wildfly:10.1.0.Final

ADD https://jdbc.postgresql.org/download/postgresql-9.4.1211.jar /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql.jar
USER root
RUN chmod 644 /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql.jar
RUN sha256sum /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql.jar | grep "^1f068169e9e11ec41200df48299d73a02c3b4a4ece237831f5f78f5b43190c8d"
ADD ./postgresql-module.xml /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/module.xml

ADD ./standalone.xml /opt/jboss/wildfly/standalone/configuration/

#https
EXPOSE 8443
#http
EXPOSE 8080
#admin Console
EXPOSE 9990
#ajp
EXPOSE 8009
#management native
EXPOSE 9999
#java debug
EXPOSE 8787

# environment variables
# use: docker run -e JAVA_OPTS="" to override

# settings for java & wildfly 10 (overriding standalone.conf)
ENV JAVA_OPTS="-Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true"

# default wildfly 10 options with Java debug
# ENV JAVA_OPTS="-Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"

ADD ./target/*.war /opt/jboss/wildfly/standalone/deployments/

# run with default settings
# CMD ["/opt/jboss/wildfly/bin/standalone.sh"]

# run with public address
# CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]

# run with public address with public management console
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
