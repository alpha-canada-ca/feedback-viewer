#!/bin/sh
export GPG_TTY=$(tty)
gpg --quiet --batch --yes --decrypt --passphrase="$APPLICATION_PROPERTIES_PASSPHRASE" \
--output ./src/main/resources/application.properties ./src/main/resources/application.properties.gpg
ls ./src/main/resources
gpg --quiet --batch --yes --decrypt --passphrase="$APPLICATION_PROPERTIES_PASSPHRASE" \
--output kubernetes/feedback-viewer-ingress.yml kubernetes/feedback-viewer-ingress.yml.gpg
ls kubernetes
