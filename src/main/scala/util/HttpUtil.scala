package util

import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils

/**
  * http请求协议，get请求
  */
object HttpUtil {
  /**
    * GET 请求
    * @param url
    * @return
    */

  def get(url:String):String={
    val client= HttpClients.createDefault()
    val httpGet = new HttpGet(url)
    //获取发送请求
    val httpResponse: CloseableHttpResponse = client.execute(httpGet)
   //处理返回请求结果，处理乱码
    EntityUtils.toString(httpResponse.getEntity,"UTF-8")
  }
}
