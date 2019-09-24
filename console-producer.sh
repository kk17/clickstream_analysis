#!/usr/bin/env bash
# bash cheat sheet: https://devhints.io/bash
set -euox pipefail
IFS=$'\n\t'

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

SAMPLE_FILE=$DIR/clickstream-sample.json
KAFKA_TOPIC=clickstream-data-event
SLEEP_SCEONDS=1

function pub {
    msg=$1
    echo "$msg" | docker exec -i kafka-clickstream kafka-console-producer \
    --broker-list kafka:9092 \
    --topic ${KAFKA_TOPIC}
}

while IFS= read -r line
do
  pub "$line"
  echo "pub: $line"
  sleep ${SLEEP_SCEONDS}
done < "$SAMPLE_FILE"
