# SimpleSparkMlPipeline



# About the project 
F
Project uses [https://www.kaggle.com/sakshigoyal7/credit-card-customers](credit card customers) data to predict churning customers. 
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



# Kafka 


## Produce
To emulate customer producing you can use on of this command: 

Just some batch portion of clients:

```bash
cat BankChurners.csv | awk '{if(NR>1 && NR<200)print}' |  kafka-console-producer --topic client_in --bootstrap-server localhost:9092
```




You can start two or more producers to view what's happening if there are several channels. 

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

