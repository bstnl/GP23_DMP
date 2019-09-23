
package Tags

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.hbase.{HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import util.TagUtils

/*
上下文标签主类
 */
object TagsContext {
  def main(args: Array[String]): Unit = {
    if(args.length!=3){
      println("目录不正确")
      sys.exit()
    }
    val Array(inputpath,docs,stopwords,day)=args
    val spark=SparkSession.builder().appName("tags").master("local")
      .getOrCreate()
    import spark.implicits._

    //调用HbaseAPI
    val load = ConfigFactory.load()
    //获取表名
    val HbaseTableName=load.getString("HBASE.tableName")
    //创建hadoop任务
    val configuration = spark.sparkContext.hadoopConfiguration
    //配置Hbase链接
    configuration.set("hbase.zookeeper.quorum",load.getString("HBASE.Host"))
    //获取connection连接
    val hbConn = ConnectionFactory.createConnection(configuration)
    val hbadmin = hbConn.getAdmin
    //判断当前表是否被使用
    if(!hbadmin.tableExists(TableName.valueOf(HbaseTableName))){
      println("当前表可用")
      //创建表对象
      val tableDescriptor = new HTableDescriptor(TableName.valueOf(HbaseTableName))
      //创建列簇
      val hColumnDescriptor= new HColumnDescriptor("tags")
      //将创建好的列簇加入到表中
      tableDescriptor.addFamily(hColumnDescriptor)
      hbadmin.createTable(tableDescriptor)
      hbadmin.close()
      hbConn.close()
    }
    val conf = new JobConf(configuration)
    //指定输出类型
    conf.setOutputFormat(classOf[TableOutputFormat])
    //指定输出到哪张表
    conf.set(TableOutputFormat.OUTPUT_TABLE,HbaseTableName)

    val df = spark.read.parquet(inputpath)
    val docsRDD: collection.Map[String, String] = spark.sparkContext
      .textFile(docs).map(_.split("\\s")).filter(_.length >= 5)
      .map(arr => (arr(4), arr(1))).collectAsMap()
    //广播字典
    val broadValue= spark.sparkContext.broadcast(docsRDD)

    val stopwordRDD= spark.sparkContext
      .textFile(stopwords).map((_,0)).collectAsMap()
    //广播字典
    val broadValue1: Broadcast[collection.Map[String,Int]] = spark.sparkContext.broadcast(stopwordRDD)

    //处理数据信息
    val res= df.rdd.map(row => {
      //获取用户唯一id
      val userId = TagUtils.getOneUserId(row)
      //标签的实现
      val adList = TagAd.makeTags(row)
      //媒体标签
      val apList= TagApp.makeTags(row,broadValue)
      //设备标签
      val devList= TagDevice.makeTags(row)
      //地域标签
      val locList = TagLocation.makeTags(row)
      //关键字标签
      val kwList = TagKword.makeTags(row,broadValue1)
      //商圈
      //val business = BusinessTag.makeTags(row)
     //kwList
      (userId,adList++apList++/*business++*/devList++locList++kwList)
    })
      .reduceByKey((list1,list2)=>{
      val tuples: List[(String, Int)] = list1:::list2
      tuples.groupBy(_._1).mapValues(_.foldLeft[Int](0)(_+_._2)).toList
    })
      .map{
        case (userId,userTags) =>{
          //设置rowkey和列，列名
          val put = new Put(Bytes.toBytes(userId))
          put.addImmutable(Bytes.toBytes("tags"),Bytes.toBytes(day),Bytes.toBytes(userTags.mkString(",")))
          (new ImmutableBytesWritable(),put)
        }
      }.saveAsHadoopDataset(conf)
    //res.foreach(println)

  }
}

