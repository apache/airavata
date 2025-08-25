.DEFAULT_GOAL := serve-dev

clean:
	rm -rf distribution

build: clean
	mkdir -p distribution && mvn clean install -DskipTests

extract: build
	cd distribution && tar -xvf apache-airavata-api-server-0.21-SNAPSHOT.tar.gz

serve: extract
	cd distribution/apache-airavata-api-server-0.21-SNAPSHOT && ./bin/airavata.sh

compile-dev:
	mvn clean compile -DskipTests -pl airavata-api

serve-dev: compile-dev
	mvn exec:java -pl airavata-api
