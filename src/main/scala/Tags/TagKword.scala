package Tags

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row
import util.Tag

object TagKword extends Tag{
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String,Int)]()
    val row = args(0).asInstanceOf[Row]
    val stopwords=args(1).asInstanceOf[ Broadcast[collection.Map[String, Int]]]
    row.getAs[String]("keywords").split("\\|")
      .filter(word=>word.length>=3&&word.length<=8&& !stopwords.value.contains("word"))
        .foreach(word=>list:+=("K"+word,1))
    list
  }
}
