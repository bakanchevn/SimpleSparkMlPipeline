package github.bakanchevn

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.ml.PipelineModel

object MLRunner {
  val spark = SparkSession.builder()
    .appName("MLRunner")
    .getOrCreate()

  val config = ConfigFactory.load()
  val bootstrapServers = config.getString("bootstrap.servers")
  val inputTopic = config.getString("input_topic")
  val outputTopic = config.getString("output_topic")
  val pathToModel = config.getString("path_to_model")
  val checkpointLocation = config.getString("checkpoint_location")


  def main(args: Array[String]): Unit = {


    import spark.implicits._

    val model = PipelineModel.load(pathToModel)

    val input = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", bootstrapServers)
      .option("subscribe", inputTopic)
      .load()

    val inputTransform = input
      .selectExpr("CAST(value as STRING)")
      .as[String]
      .map(_.replace("\"", "").split(","))
      .map(Record(_))

//
//    inputTransform.writeStream.format("console").start()

    val prediction = model.transform(inputTransform)

    val query = prediction
      .select($"CLIENTNUM".cast("string").as("key"), $"prediction".cast("string").as("value") )
      .writeStream
      .option("checkpointLocation", checkpointLocation)
      .outputMode("append")
      .format("kafka")
      .option("kafka.bootstrap.servers", bootstrapServers)
      .option("topic", outputTopic)
      .start()

    query.awaitTermination()
  }
}
