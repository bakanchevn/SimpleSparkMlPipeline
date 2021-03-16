package github.bakanchevn

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{ChiSqSelector, MinMaxScaler, OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object MLModelGeneration {

  val spark = SparkSession.builder()
    .appName("MlModel")
    .getOrCreate()




  def main(args: Array[String]): Unit = {
    val df = getDataFrame()
    val numericCols = getNumericColumns(df)
    val data = transformData(df)
    val stringColumns = getStringColumns(data)
    val stringIndexedColumns = getIndexedColumns(data)
    val categColumns = getCategColumns(data)
    val indexer = getIndexer(stringColumns, stringIndexedColumns)
    val encoder = getEncoder(stringIndexedColumns, categColumns)
    val assembler = getFeaturesAssembler(categColumns, numericCols)
    val normalizer = getNormalizer()
    val selector = getFeatureSelecter()
    val model = getModel()
    val pipeline = new Pipeline().setStages(Array(
      indexer, encoder, assembler, normalizer, selector, model
    ))

    val ttData = data.randomSplit(Array(0.7, 0.3))
    val trainingData = ttData(0)
    val testData = ttData(1)

    val pipelineModel = pipeline.fit(trainingData)
    pipelineModel.write.overwrite.save("pipelineModel")
  }

  def getDataFrame(): DataFrame = {
      val raw = spark
        .read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("src/main/resources/BankChurners.csv")

      val columns: Array[String] = raw.columns
      val columnsLen: Int = columns.length
      val colsToDrop: Array[String] = columns.slice(columnsLen - 2, columnsLen) ++ Array("CLIENTNUM")
      val df = raw.drop(colsToDrop: _*)

      df
    }


  def getStringColumns(df: DataFrame): Array[String] = {
    df
      .dtypes
      .filter(_._2.equals("StringType"))
      .map(_._1)
      .filter(!_.equals("Attrition_Flag"))
  }

  def getIndexedColumns(df: DataFrame): Array[String] = {
    getStringColumns(df).map(_ + "_Indexed")
  }

  def getCategColumns(df: DataFrame): Array[String] = {
    getStringColumns(df).map(_ + "_Coded")
  }

  def getNumericColumns(df: DataFrame): Array[String] = {
    val numericColumns = df
      .dtypes
      .filter(!_._2.equals("StringType"))
      .map(_._1)

    val pairs = numericColumns
      .flatMap(el1 => numericColumns.map(el2 => (el1, el2)))
      .filter(pair => !pair._1.equals(pair._2))
      .map { pair => if (pair._1 < pair._2) (pair._1, pair._2) else (pair._2, pair._1)}
      .distinct

    val corr = pairs
      .map { pair => (pair._1, pair._2, df.stat.corr(pair._1, pair._2))}
      .filter(_._3 > 0.6)

    numericColumns.diff(corr.map(_._2))
  }

  def transformData(df: DataFrame): DataFrame = {
    import spark.implicits._

    val dft = df.withColumn("target", when($"Attrition_Flag" === "Existing Customer", 0).otherwise(1))
    val df1 = dft.filter($"target" === 1)
    val df0 = dft.filter($"target" === 0)
    val df1count = df1.count
    val df0count = df0.count
    val df1Over = df1
      .withColumn("dummy", explode(lit((1 to (df0count / df1count).toInt).toArray)))
      .drop("dummy")
    df0.unionAll(df1Over)
  }


  def getIndexer(stringColumns: Array[String], stringColumnsIndexed: Array[String]): StringIndexer = {
    new StringIndexer()
      .setInputCols(stringColumns)
      .setOutputCols(stringColumnsIndexed)
  }

  def getEncoder(stringColumnsIndexed: Array[String], categColumns: Array[String]): OneHotEncoder = {
    new OneHotEncoder()
      .setInputCols(stringColumnsIndexed)
      .setOutputCols(categColumns)
  }

  def getFeaturesAssembler(categColumns: Array[String], numericColumns: Array[String]): VectorAssembler = {

    val featureCols = numericColumns ++ categColumns

    new VectorAssembler()
      .setInputCols(featureCols)
      .setOutputCol("features")
  }

  def getNormalizer(): MinMaxScaler = {
    new MinMaxScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")
  }

  def getFeatureSelecter(): ChiSqSelector = {
    new ChiSqSelector()
      .setNumTopFeatures(10)
      .setFeaturesCol("scaledFeatures")
      .setLabelCol("target")
      .setOutputCol("selectedFeatures")

  }

  def getModel(): LogisticRegression =  {
    new LogisticRegression()
    .setMaxIter(1000)
    .setRegParam(0.01)
    .setElasticNetParam(0.0)
    .setFeaturesCol("selectedFeatures")
    .setLabelCol("target")
  }






}
