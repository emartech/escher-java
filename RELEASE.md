- execute maven goal - package
- generate sources.jar: mvn source:jar
- sign all files: gpg -ab pom.xml, ...
- bundle: jar -cvf bundle.jar pom.xml pom.xml.asc escher-0.1.jar escher-0.1.jar.asc escher-0.1-javadoc.jar escher-0.1-javadoc.jar.asc escher-0.1-sources.jar escher-0.1-sources.jar.asc
- upload to https://oss.sonatype.org/#welcome

see more info:
http://central.sonatype.org/pages/ossrh-guide.html
http://central.sonatype.org/pages/requirements.html
https://issues.sonatype.org/browse/OSSRH-13682