#!/bin/bash
set -e -u

#
# perform local install
#

cd "${BASH_SOURCE%/*}/.."

git add -A
git commit -m "install"

./mvnw.sh clean install -B
