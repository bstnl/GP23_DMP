package util

import org.apache.spark.sql.SparkSession

object Test {
  def main(args: Array[String]): Unit = {
    val spark=SparkSession.builder().master("local").appName("test").getOrCreate()
  /*  val arr = Array("https://restapi.amap.com/v3/geocode/regeo?&location=116.310003,39.991957&key=d0026e3e778e1af9aa977111077c5ab7&radius=1000&extensions=all")

    val rdd = spark.sparkContext.makeRDD(arr)
    rdd.map(t=>{
      HttpUtil.get(t)
    })
      .foreach(println)

*/
    import spark.implicits._
    val df = spark.read.parquet("F:\\gp23dmp")
    df.map(row=>{
      AmapUtil.getBusinessFromAmap(
        String2Type.toDouble(row.getAs[String]("long")),
        String2Type.toDouble(row.getAs[String]("lat"))
      )
    }).rdd.foreach(println)
  }
}
