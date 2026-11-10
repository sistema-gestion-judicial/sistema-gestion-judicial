# syntax=docker/dockerfile:1
# Oficialía image: builds the WAR and runs it on Tomcat 10.1.
# Build it from the repo root, because Oficialía needs the common module:
#   docker build -f oficialia/Dockerfile -t sgj-oficialia .

# ---------- Stage 1: build the WAR with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
# The cache mount keeps ~/.m2 between builds, so dependencies are downloaded only once
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl oficialia -am -DskipTests package
# Unpack the WAR here: at runtime the container user may not be allowed to write to webapps/
RUN mkdir /app && cd /app && jar -xf /src/oficialia/target/oficialia.war

# ---------- Stage 2: only Tomcat, the JRE and the app ----------
FROM tomcat:10.1-jre21-temurin-noble
# The official image starts with webapps/ empty; ROOT publishes the app at "/"
COPY --from=build /app /usr/local/tomcat/webapps/ROOT

# 1) Tomcat replaces ${port.http} in server.xml with a Java system property set when it starts.
# 2) Heroku runs the container as a random non-root user: Tomcat must be able to write its folders.
RUN sed -i 's/port="8080"/port="${port.http}"/' conf/server.xml \
 && mkdir -p conf/Catalina/localhost \
 && chmod -R a+rwX logs work temp conf/Catalina webapps

# Same memory limits Heroku uses for Java on 512 MB dynos, and Mexico City time for dates
ENV CATALINA_OPTS="-Xmx300m -Xss512k -XX:CICompilerCount=2 -Duser.timezone=America/Mexico_City"

# Documentation only: Heroku ignores EXPOSE and assigns the port in $PORT
EXPOSE 8080
CMD ["sh", "-c", "export CATALINA_OPTS=\"$CATALINA_OPTS -Dport.http=${PORT:-8080}\" && exec catalina.sh run"]
