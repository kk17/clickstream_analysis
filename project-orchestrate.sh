#!/usr/bin/env bash
# bash cheat sheet: https://devhints.io/bash
set -euo pipefail
IFS=$'\n\t'

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker exec cassandra-clickstream cqlsh --username cassandra --password cassandra  -f /schema.cql
docker exec kafka-clickstream kafka-topics --create --topic clickstream-data-event --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

