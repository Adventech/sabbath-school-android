#!/bin/bash

decrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --quiet --batch --yes --decrypt --passphrase="$PASSPHRASE" --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  # Decrypt Release key
  decrypt ${ENCRYPT_KEY} release/v2_key.jks.gpg release/v2_key.jks
  # Decrypt Play Store key
  decrypt ${ENCRYPT_KEY} release/play-account.json.gpg release/play-account.json
  # Decrypt keystore.properties
  decrypt ${ENCRYPT_KEY} release/keystore.properties.gpg release/keystore.properties
else
  echo "ENCRYPT_KEY is empty"
fi