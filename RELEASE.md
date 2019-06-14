# Release new version

- increment version in `pom.xml`
- compile and package the project:
```bash
mvn clean package source:jar
```
- import gpg key to sign files
  - key file: https://secret.emarsys.net/cred/detail/2542/
```bash
gpg --import sonatype_emartech_gpg_private.key
```
- sign files
  - key passphrase: https://secret.emarsys.net/cred/detail/2542/
```bash
cp pom.xml target
cd target

gpg  -ab --default-key 41EBF74D9F93DA29 pom.xml
gpg  -ab --default-key 41EBF74D9F93DA29 escher-0.3.2.jar
gpg  -ab --default-key 41EBF74D9F93DA29 escher-0.3.2-javadoc.jar
gpg  -ab --default-key 41EBF74D9F93DA29 escher-0.3.2-sources.jar
```
- bundle lib
```bash
jar -cvf bundle.jar pom.xml pom.xml.asc escher-0.3.2.jar escher-0.3.2.jar.asc escher-0.3.2-javadoc.jar escher-0.3.2-javadoc.jar.asc escher-0.3.2-sources.jar escher-0.3.2-sources.jar.asc
```
- upload bundle.jar to https://oss.sonatype.org/ password: https://secret.emarsys.net/cred/detail/2473/
- wait
- press release on the staging repo created for the bundle
- wait until is it automatically deployed to maven central

## see more info:
- http://central.sonatype.org/pages/ossrh-guide.html
- http://central.sonatype.org/pages/requirements.html
- https://issues.sonatype.org/browse/OSSRH-13682
