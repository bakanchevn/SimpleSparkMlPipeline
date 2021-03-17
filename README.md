# SimpleSparkMlPipeline



# About the project 

Project uses [credut card customers](https://www.kaggle.com/sakshigoyal7/credit-card-customers) data to predict churning customers. 
It makes SparkML model, and then Using it's model inside Spark Streaming Application which reading data from one Kafka topic and produce the result to another.



Project consists of 4 docker images:
- Kafka
- Spark-master
- Spark-worker-1
- Zookeper

For starting all images use 

```bash
docker-compose up
```



## Prerequisite 

JAR should be assembled in [StreamingProject](StreamingProject/build.sbt)

# Spark-worker-1

This image has 2 spark applications:

## Model builder 

To build a model you can run inside a docker:

```bash
sh spark_model_run.sh
```

It will run spark-submit for [github.bakanchevn.MLModelGeneration class](StreamingProject/src/main/scala/github/bakanchevn/MLModelGeneration.scala)

It builds model and save to the file, which can be reused after. 


## Streaming process starter 

To start a streaming pipeline which gives the result of incoming client information run this command:

```bash
sh spark_stream_run.sh
```

It starts application which use stream which reads topic client_in from kafkas, provides model evaulation for rows, and produces result to topic client_out.



# Kafka 


## Produce
To emulate customer clients flow input you can use one of these commands: 

Just some batch portion of clients:

```bash
cat BankChurners.csv | awk '{if(NR>1 && NR<200)print}' |  kafka-console-producer --topic client_in --bootstrap-server localhost:9092
```


If you want to run each row with some latency it can be done by 

```bash 
awk -F ',' 'NR>1 {print}' < BankChurners.csv | xargs -I % sh  -c '{ echo %; sleep 1; }' |  kafka-console-producer --topic client_in --bootstrap-server localhost:9092
```

If there is no xargs in docker, you can use [bash script](KafkaFiles/kafka_produce_with_latency_no_xargs.sh)



You can start two or more kafka-console-producers via provided scripts to view what's happening if there are several channels. 

## Consume 
To see results of model evaluation you can use kafka-console-consumer
Example: 

```bash
kafka-console-consumer --topic client_out --bootstrap-server localhost:9092 --property print.key=true --property key.separator="-" --from-beginning
```




# Improvements 

- Set up correct spark master standalone cluster instead of local run
- Correct docker:
  - Make sbt build (or dockerizing) instead of getting a jar before build 
- Add application, which will produce data to client_in topic and consume data from client_out topic inside it own thread
- Change data model for uniqueness. Right now, if you send to producer the same client from both parts, you hardly can understand which result of whose. 

