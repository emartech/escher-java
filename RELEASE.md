# Release new version

## Preparation

- fetch necessary information from [here](https://secret.emarsys.net/cred/detail/2542/)
- add private key file to the project root named as `sonatype_emartech_gpg_private.key`
- set required environment variables
  - `cp .env.example .env`
  - update passphrase
  - update package version

## Create bundle.jar

```
make bundle
```

## Upload bundle.jar

- Log in to [Nexus](https://oss.sonatype.org/) with [these credentials](https://secret.emarsys.net/cred/detail/2473/)
- Upload `bundle.jar` at [staging upload](https://oss.sonatype.org/#staging-upload)
  - change upload mode to "Artifact Bundle"
  - select `bundle.jar` in the "Select Bundle to Upload" dialogue
  - click "Upload Bundle"
- Release new bundle
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

