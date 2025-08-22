.DEFAULT_GOAL := all

clean:
	rm -rf distribution

build: clean
	mkdir -p distribution && mvn clean install -DskipTests

extract: build
	cd distribution && tar -xvf apache-airavata-api-server-0.21-SNAPSHOT.tar.gz

all: extract
	cd distribution/apache-airavata-api-server-0.21-SNAPSHOT && ./bin/airavata.sh
