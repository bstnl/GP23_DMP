package Tags

import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import util.Tag

object TagAd extends Tag{

  override def makeTags(args: Any*): List[(String, Int)] = {

    var list =List[(String,Int)]()
    // 获取数据类型
    val row = args(0).asInstanceOf[Row]
    // 获取广告位类型和名称
    val adType: Int = row.getAs[Int]("adspacetype")
    // 广告位类型标签
    // 广告名称

    adType match {
      case v if v > 9 => list:+=("LC"+v,1)
      case v if v > 0 && v <= 9 => list:+=("LC0"+v,1)
    }
    val adName = row.getAs[String]("adspacetypename")
    if(StringUtils.isNotBlank(adName)){
      list:+=("LN"+adName,1)
    }
//渠道标签
    val channel = row.getAs[Int]("adplatformproviderid")
    list:+=("CN"+channel,1)
    list

  }
}





/*object TagAd {
  def main(args: Array[String]): Unit = {
    if(args.length!=2){
      println("输入目录不正确")
      sys.exit()
    }
    val Array(inputPath,outputPath)=args

    val spark =SparkSession.builder().appName("ct").master("local")
      .config("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val df = spark.read.parquet(inputPath)
    /*var list =List[(String,Int)]()
    val res = df.rdd.map(row => {
      var adspacetype = row.getAs[Int]("adspacetype").toString
      if (adspacetype.length == 1) {
        adspacetype = "0" + adspacetype
      }
      adspacetype = "LC" + adspacetype
      (adspacetype, 1)
    })*/
    //res.collect().foreach(println)

    res.collect.foreach(println)

  }
}*/
