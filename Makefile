all:
	rm -rf distribution && \
	mkdir -p distribution && \
	mvn clean install -DskipTests && \
	cd distribution && \
	tar -xvf apache-airavata-api-server-0.21-SNAPSHOT.tar.gz && \
	cd apache-airavata-api-server-0.21-SNAPSHOT && \
	./bin/airavata.sh
