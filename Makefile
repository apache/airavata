.DEFAULT_GOAL := serve

build:
	mvn clean install -DskipTests -T4

serve: build
	java -jar airavata-server/target/airavata-server-0.21-SNAPSHOT.jar

compile:
	mvn clean compile -DskipTests -T4

serve-dev: compile
	mvn spring-boot:run -pl airavata-server

test:
	mvn test -T4
