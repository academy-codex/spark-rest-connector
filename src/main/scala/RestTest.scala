import org.apache.spark.sql.SparkSession

object RestTest {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[1]")
      .appName("SparkByExample")
      .getOrCreate()

    println("APP Name :" + spark.sparkContext.appName)
    println("Deploy Mode :" + spark.sparkContext.deployMode)
    println("Master :" + spark.sparkContext.master)

    val uri = "https://httpbin.org/get"

    val sodainput1 = ("103.95.83.175", "https://httpbin.org/get")
    val sodainputRdd = spark.sparkContext.parallelize(Seq(sodainput1))
//    val sodainputRdd = spark.sparkContext.parallelize(Seq())

    val sodainputKey1 = "origin"
    val sodainputKey2 = "url"

    import spark.implicits._
    val sodaDf = sodainputRdd.toDF(sodainputKey1, sodainputKey2)
    sodaDf.createOrReplaceTempView("sodainputtbl")

    val df = spark.read.format("rest.RestDataSource")
      .option("url", uri)
      .option("input", "sodainputtbl")
      .option("method", "GET")
      .option("readTimeout", "10000")
      .option("connectionTimeout", "2000")
      .option("partitions", "10")
      .load()

    df.show(truncate = false)

  }
}
