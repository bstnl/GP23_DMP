package util

trait Tag {
  def makeTags(args:Any*):List[(String,Int)]
}
