package Tags

import org.apache.commons.lang3.StringUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{Row, SparkSession}
import util.Tag

object TagApp extends Tag{
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list =List[(String,Int)]()
    val row = args(0).asInstanceOf[Row]
    val appmap = args(1).asInstanceOf[Broadcast[collection.Map[String, String]]]
    val appname = row.getAs[String]("appname")
    val appid = row.getAs[String]("appid")
    //空值判断
    if(StringUtils.isNotBlank(appname)) {
      list :+= ("APP" + appname, 1)
    }
    else {
      list:+=("APP"+appmap.value.getOrElse(appid,appid),1)
    }
    list
  }
}


/*
def main(args: Array[String]): Unit = {
  if(args.length!=2){
  println("输入目录不正确")
  sys.exit()
}
  val Array(inputPath,outputPath,docs)=args
  val spark =SparkSession.builder().appName("ct").master("local")
  .config("spark.serializer","org.apache.spark.serializer.KryoSerializer")
  .getOrCreate()

  val df = spark.read.parquet(inputPath)
  val docMap = spark.sparkContext.textFile(docs).map(_.split("\\s"))
  .filter(_.length >= 5).map(arr => (arr(1), arr(4))).collectAsMap()
  val broadcastdoc = spark.sparkContext.broadcast(docMap)

  df.rdd.map(row=>{
  val appName = row.getAs("appname")

})
}*/
