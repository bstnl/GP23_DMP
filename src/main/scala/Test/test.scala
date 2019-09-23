package Test

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object test {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(this.getClass.getName)
    val sc = new SparkContext(conf)
    val a: RDD[Int] = sc.parallelize(List(1,2,3,1,2),1)
    val b: RDD[String] = sc.parallelize(List("a","b","c","b","d"),1)
    val c: RDD[(Int, String)] = a.zip(b)
    //c.foreach(println)
    //c.collectAsMap().foreach(println)

  }
}













