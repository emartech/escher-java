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

gpg -ab --default-key 41EBF74D9F93DA29 pom.xml
gpg -ab --default-key 41EBF74D9F93DA29 escher-${NEW_VERSION_NUMBER}.jar
gpg -ab --default-key 41EBF74D9F93DA29 escher-${NEW_VERSION_NUMBER}-javadoc.jar
gpg -ab --default-key 41EBF74D9F93DA29 escher-${NEW_VERSION_NUMBER}-sources.jar
```
- bundle lib
```bash
jar -cvf bundle.jar pom.xml pom.xml.asc escher-${NEW_VERSION_NUMBER}.jar escher-${NEW_VERSION_NUMBER}.jar.asc escher-${NEW_VERSION_NUMBER}-javadoc.jar escher-${NEW_VERSION_NUMBER}-javadoc.jar.asc escher-${NEW_VERSION_NUMBER}-sources.jar escher-${NEW_VERSION_NUMBER}-sources.jar.asc
```
- log in to https://oss.sonatype.org/, creds: https://secret.emarsys.net/cred/detail/2473/
- upload bundle.jar at https://oss.sonatype.org/#staging-upload, upload mode -> Artifact Bundle
- go to https://oss.sonatype.org/#stagingRepositories
- wait a bit until the release button become available
- press release on the staging repo created for the bundle
- wait until is it automatically deployed to maven central (it can take up to 2 hours to appear in MC)

## see more info:
- http://central.sonatype.org/pages/ossrh-guide.html
- http://central.sonatype.org/pages/requirements.html
- https://issues.sonatype.org/browse/OSSRH-13682
