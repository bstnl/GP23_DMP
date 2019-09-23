package Location

import org.apache.spark.sql.SparkSession

object distribution {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("目录不正确，推出程序")
      sys.exit()
    }
    val Array(inputPath, outputPath) = args
    val spark = SparkSession.builder()
      .appName("ct")
      .master("local")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val df = spark.read.parquet(inputPath)
    df.createTempView("log")
    val df2 =spark.sql("select provincename,cityname," +
      " max(case when requestmode = 1 and processnode >=1 then ' ' end) AS `原始请求` ," +
      " max(case when requestmode = 1 and processnode >=2 then ' ' else ' ' end) AS `有效请求数`," +
      " max(case when requestmode = 1 and processnode =3 then ' ' else ' ' end) AS `广告请求数`," +
      " sum(case when iseffective = 1 and isbilling = 1 and isbid = 1 then 1 else 0 end) AS a " +
      " sum(case when iseffective = 1 and isbilling = 1 and iswin = 1 and adorderid != 0 then 1 else 0 end) AS b" +
     /* "max(case when iseffective = 1 and isbilling = 1 and isbid = 1 then (a/b) else 0 end) AS `竞价成功率`" +
      "sum(case when requestmode = 3 and iseffective = 1 then 1 else 0 end) AS c" +
      "sum(case when iseffective = 1 and isbilling = 1 and iswin = 1 then 1 else 0 end) AS d " +
      "sum(case when )"+*/
      " from log group by provincename,cityname order by provincename")

    df2.show()

    spark.stop()

  }




}
