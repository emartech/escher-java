# Release new version

## Preparation

- Make sure version was package incremented in `pom.xml`
- Compile and package by running `mvn clean package source:jar`

## Import GPG key

You can find the private key file [here](https://secret.emarsys.net/cred/detail/2542/)

```bash
gpg --import sonatype_emartech_gpg_private.key
```

## Sign files

Signing will prompt you for a key passphrase which can be found [here](https://secret.emarsys.net/cred/detail/2542/)

```bash
cp pom.xml target
cd target

gpg -ab --default-key 41EBF74D9F93DA29 pom.xml
gpg -ab --default-key 41EBF74D9F93DA29 escher-${NEW_VERSION_NUMBER}.jar
gpg -ab --default-key 41EBF74D9F93DA29 escher-${NEW_VERSION_NUMBER}-javadoc.jar
gpg -ab --default-key 41EBF74D9F93DA29 escher-${NEW_VERSION_NUMBER}-sources.jar
```

## Create bundle.jar

```bash
jar -cvf bundle.jar pom.xml pom.xml.asc escher-${NEW_VERSION_NUMBER}.jar escher-${NEW_VERSION_NUMBER}.jar.asc escher-${NEW_VERSION_NUMBER}-javadoc.jar escher-${NEW_VERSION_NUMBER}-javadoc.jar.asc escher-${NEW_VERSION_NUMBER}-sources.jar escher-${NEW_VERSION_NUMBER}-sources.jar.asc
```

## Upload bundle.jar

- Log in to [Nexus](https://oss.sonatype.org/) with [these creds](https://secret.emarsys.net/cred/detail/2473/)
- Upload `bundle.jar` at [stating upload](https://oss.sonatype.org/#staging-upload), 
  - change upload mode to "Artifact Bundle"
- go to [staging repositories](https://oss.sonatype.org/#stagingRepositories)
- wait a bit until the "release" button becomes available
- press release on the staging repo created for the bundle
- wait until is it automatically deployed to maven central (it can take up to 2 hours to appear in MC)

## More info

- [OSS hosting intro](http://central.sonatype.org/pages/ossrh-guide.html)
- [Requirements (Sonatype)](http://central.sonatype.org/pages/requirements.html)
- [Working with GPG Signature](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)
  - [Dealing with Expired Keys](https://central.sonatype.org/publish/requirements/gpg/#dealing-with-expired-keys)
  - [Distributing Public key](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)
- [Jira ticket for adding Escher-java](https://issues.sonatype.org/browse/OSSRH-13682)
- [Emarsys public keys on mit.edu](https://pgp.mit.edu/pks/lookup?search=emarsys&op=index)
  - [Sonatype Emartech public key](https://pgp.mit.edu/pks/lookup?op=get&search=0x41EBF74D9F93DA29)
