#!/bin/bash

GPG_PRI_KEY_FILE=sonatype_emartech_gpg_private.key
DEFAULT_KEY=41EBF74D9F93DA29

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

cp pom.xml target
cd target

echo Sign files...
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} pom.xml
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}.jar
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}-javadoc.jar
gpg -ab --pinentry-mode=loopback --passphrase ${GPG_PASSPHRASE} --default-key ${DEFAULT_KEY} escher-${NEW_VERSION_NUMBER}-sources.jar

echo Create bundle...
jar -cvf bundle.jar \
  pom.xml pom.xml.asc \
  escher-${NEW_VERSION_NUMBER}.jar escher-${NEW_VERSION_NUMBER}.jar.asc \
  escher-${NEW_VERSION_NUMBER}-javadoc.jar escher-${NEW_VERSION_NUMBER}-javadoc.jar.asc \
  escher-${NEW_VERSION_NUMBER}-sources.jar escher-${NEW_VERSION_NUMBER}-sources.jar.asc

