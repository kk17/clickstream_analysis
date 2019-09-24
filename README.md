# Clickstream Analysis

## Architecture

[Architecture](./architecture/Solution.md)

## How to use

*  Set the KAFKA_ADVERTISED_LISTENERS with your IP in the docker-compose.yml
* `mvn package`
* `docker network create -d bridge --subnet 172.23.0.0/24 clickstream_analysis_net`
* `docker-compose up -d`
*  Wait all services be up and running, then...
* `./project-orchestrate.sh`
* Run realtime job `docker exec spark-master-clickstream /spark/bin/spark-submit --class net.kk17.clickstream.spark.processor.StreamingProcessor  --master spark://spark-master:7077 /opt/spark-data/spark-processor-1.0.0.jar`
* Run the clickstream sample data console producer `./console-producer.sh`

This project is reference to [apssouza22/lambda-arch](https://github.com/apssouza22/lambda-arch)
