FROM netflixoss/exhibitor:1.5.2

ADD exhibitor.properties /exhibitor/exhibitor.properties

ENTRYPOINT ["java", "-jar", "exhibitor-1.0-jar-with-dependencies.jar", "-c", "file", "--defaultconfig", "/exhibitor/exhibitor.properties"]

