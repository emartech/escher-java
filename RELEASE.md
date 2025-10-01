# Release new version

## Preparation

- fetch necessary information from [here](https://secret.emarsys.net/cred/detail/2542/)
- add private key file to the project root named as `sonatype_emartech_gpg_private.key`
- set required environment variables
  - `cp .env.example .env`
  - update passphrase
  - update package version in `.env: NEW_VERSION_NUMBER`

## Create bundle.jar

```
make bundle
```

## Upload bundle.jar

- Log in to [Central Portal](https://central.sonatype.com) with [these credentials](https://secret.emarsys.net/cred/detail/2473/)
- Upload `bundle.jar` at [Deployments](https://central.sonatype.com/publishing/deployments) under `com.escher` namespace manually
  - click on "Publish Component" under "com.escher" namespace
  - name the deployment as `com.emarsys:escher:{NEW_VERSION}` (example: `com.emarsys:escher:1.1.1`)
  - scroll down and click "Upload Your File" and upload the `bundle.jar`
- Release new bundle with clicking the "Publish" button
  - wait until is it automatically deployed to maven central (it can take up to 2 hours to appear in MC)

## More info

- [Escher versions](https://central.sonatype.com/artifact/com.emarsys/escher/versions)
- [Escher Maven Central](https://mvnrepository.com/artifact/com.emarsys/escher)
- [Central Publisher Portal hosting intro](https://central.sonatype.org/publish/publish-portal-guide/)
- [Working with GPG Signature](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)
  - [Dealing with Expired Keys](https://central.sonatype.org/publish/requirements/gpg/#dealing-with-expired-keys)
  - [Distributing Public key](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)
- [Jira ticket for adding Escher-java](https://issues.sonatype.org/browse/OSSRH-13682)
- [Emarsys public keys on mit.edu](https://pgp.mit.edu/pks/lookup?search=emarsys&op=index)
  - [Sonatype Emartech public key](https://pgp.mit.edu/pks/lookup?op=get&search=0x41EBF74D9F93DA29)

