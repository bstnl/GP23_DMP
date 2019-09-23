package util

import java.lang

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}

import scala.collection.mutable.ListBuffer

/**
  * 从高德地图获取商圈信息
  */
object AmapUtil {
  /**
    * 解析经纬度
    * @param long
    * @param lat
    * @return
    */
  def getBusinessFromAmap(long:Double,lat:Double):String={
    //https://restapi.amap.com/v3/geocode/regeo?
    //location=116.310003,39.991957&key=d0026e3e778e1af9aa977111077c5ab7&radius=1000&extensions=all
    val location =long+","+lat
    val url ="/https://restapi.amap.com/v3/geocode/regeo?location="+location+"&key=d0026e3e778e1af9aa977111077c5ab7"

    //调用http接口发送请求
    val jsonstr: String = HttpUtil.get(url)
    //解析json串
    val jSONObject1: JSONObject = JSON.parseObject(jsonstr)
    //判断当前状态是否为1
    val status: Int = jSONObject1.getIntValue("status")
    if(status==0) return ""
    //如果不为空
    val jSONObject = jSONObject1.getJSONObject("regeocode")
    if(jSONObject == null) return ""
    val jSONObject2: JSONObject = jSONObject.getJSONObject("addressComponent")
    if (jSONObject2 == null) return ""
    val jSONArray: JSONArray = jSONObject2.getJSONArray("businessAreas")
    if(jSONArray==null) return ""
    //定义集合取值
    val result: ListBuffer[String] = collection.mutable.ListBuffer[String]()
    //循环数组
    for(item<-jSONArray.toArray()){
      if(item.isInstanceOf[JSONObject]){
        //数组内元素为json串
        val json: JSONObject = item.asInstanceOf[JSONObject]
        val name: String = json.getString("name")
        result.append(name)
      }
    }
    result.mkString(",")
  }
}
