package Location

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession
import org.apache.commons.lang3.StringUtils
import util.RptUtils

/**
  * 媒体分析指标
  */
class APP {

}
object APP{
  def main(args: Array[String]): Unit = {
    if (args.length !=3){
      println("目录不正确，推出程序")
      sys.exit()
    }
    val Array(inputPath,outputPath,docs)=args
    val spark=SparkSession.builder()
      .appName("ct")
      .master("local")
      .config("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    //读取数据字典
    val docMap: collection.Map[String, String] = spark.sparkContext.textFile(docs).map(_.split("\\s", -1))
      .filter(_.length >= 5).map(arr => (arr(4), arr(1))).collectAsMap()
    val broadcast: Broadcast[collection.Map[String, String]] = spark.sparkContext.broadcast(docMap)
    //读取本地数据文件
     val df = spark.read.parquet(inputPath)
    df.rdd.map(row=>{
      var appName = row.getAs[String]("appname")
      if(StringUtils.isBlank(appName)){
        appName=broadcast.value.getOrElse(row.getAs("appid"),"unknow")
      }
      //根据指标的字段获取数据
      //requestmode processnode iseffective isbilling isbid iswin adorderid winprice adpayment
      val requestmode = row.getAs[Int]("requestmode")
      val processnode = row.getAs[Int]("processnode")
      val iseffective = row.getAs[Int]("iseffective")
      val isbilling = row.getAs[Int]("isbilling")
      val isbid = row.getAs[Int]("isbid")
      val iswin = row.getAs[Int]("iswin")
      val adorderid = row.getAs[Int]("adorderid")
      val winprice = row.getAs[Double]("winprice")
      val adpayment = row.getAs[Double]("adpayment")

      val reptList = RptUtils.ReqPt(requestmode,processnode)
      val clickList = RptUtils.clickPt(requestmode,iseffective)
      val adList=RptUtils.adPt(iseffective,isbilling,isbid,iswin,adorderid,winprice,adpayment)
      val allist: List[Double] = reptList++clickList++adList
      (appName,allist)
    }).reduceByKey((x,y)=>{
      x.zip(y).map(t=>t._1+t._2)
    })
      .map(t=>t._1+","+t._2.mkString(","))
      .saveAsTextFile(outputPath)



  }
}