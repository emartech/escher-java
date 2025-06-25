#!/bin/bash

GPG_PRI_KEY_FILE=sonatype_emartech_gpg_private.key
DEFAULT_KEY=41EBF74D9F93DA29
BUNDLE_DIRECTORY="com/emarsys/escher/${NEW_VERSION_NUMBER}"

if [[ -z "${NEW_VERSION_NUMBER}" ]] ; then
  echo "ERROR: New version number environment variable must be set!"
  exit 1
fi

if [[ -z "${GPG_PASSPHRASE}" ]]; then
  echo "ERROR: GPG passphrase environment variable must be set!"
  exit 1
fi

if [[ ! -f "${GPG_PRI_KEY_FILE}" ]]; then
  ls
  echo "ERROR: ${GPG_PRI_KEY_FILE} must be provided!"
  exit 1
fi

echo Bump package version...
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${NEW_VERSION_NUMBER} -q

echo Building release...
mvn clean package source:jar -q

echo Importing gpg key...
gpg --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --import ${GPG_PRI_KEY_FILE}

cp pom.xml target/escher-${NEW_VERSION_NUMBER}.pom
cd target

echo Sign files...
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}.pom
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}.jar
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}-javadoc.jar
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}-sources.jar
sha1sum escher-${NEW_VERSION_NUMBER}.pom | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}.pom.sha1
sha1sum escher-${NEW_VERSION_NUMBER}.jar | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}.jar.sha1
sha1sum escher-${NEW_VERSION_NUMBER}-javadoc.jar | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}-javadoc.jar.sha1
sha1sum escher-${NEW_VERSION_NUMBER}-sources.jar | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}-sources.jar.sha1
md5sum escher-${NEW_VERSION_NUMBER}.pom | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}.pom.md5
md5sum escher-${NEW_VERSION_NUMBER}.jar | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}.jar.md5
md5sum escher-${NEW_VERSION_NUMBER}-javadoc.jar | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}-javadoc.jar.md5
md5sum escher-${NEW_VERSION_NUMBER}-sources.jar | cut -d ' ' -f 1 > escher-${NEW_VERSION_NUMBER}-sources.jar.md5

echo Setting up bundle directory...
mkdir -p ${BUNDLE_DIRECTORY}
cp escher-${NEW_VERSION_NUMBER}.pom* ${BUNDLE_DIRECTORY}
cp escher-${NEW_VERSION_NUMBER}.jar* ${BUNDLE_DIRECTORY}
cp escher-${NEW_VERSION_NUMBER}-javadoc.jar* ${BUNDLE_DIRECTORY}
cp escher-${NEW_VERSION_NUMBER}-sources.jar* ${BUNDLE_DIRECTORY}

echo Create bundle...
jar -cvfM bundle.jar com
