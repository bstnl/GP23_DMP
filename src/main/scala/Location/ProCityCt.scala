package Location

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{SaveMode, SparkSession}

object ProCityCt {
  def main(args: Array[String]): Unit = {
    if (args.length !=2){
      println("目录不正确，推出程序")
      sys.exit()
    }
    val Array(inputPath,outputPath)=args
    val spark=SparkSession.builder()
      .appName("ct")
      .master("local")
      .config("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val df = spark.read.parquet(inputPath)
    df.createTempView("log")
    val df2 = spark
      .sql("select provincename,cityname,count(*) ct from log group by provincename,cityname")

    //df2.write.partitionBy("provincename","cityname").json("spark-warehouse/procity")

    //通过config配置文件依赖加载相关的配置信息
    val load = ConfigFactory.load()
    val pro = new Properties()

    pro.setProperty("user",load.getString("jdbc.user"))
    pro.setProperty("password",load.getString("jdbc.password"))

    df2.write.mode(SaveMode.Append).jdbc(load.getString("jdbc.url"),load.getString("jdbc.tableName"),pro)

    spark.stop()

  }

}
