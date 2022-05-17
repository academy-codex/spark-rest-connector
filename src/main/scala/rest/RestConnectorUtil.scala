package rest

import java.io.InputStream
import java.net.{HttpURLConnection, URL, URLEncoder}
import scala.annotation.switch
import scala.collection.mutable.ArrayBuffer

import scalaj.http.{Http, HttpOptions, Token}

object RestConnectorUtil {


  def callRestAPI(uri: String,
                  data: String,
                  method: String,
                  oauthCredStr: String,
                  userCredStr: String,
                  connStr: String,
                  contentType: String,
                  respType: String,
                  authToken: String): Any = {


    // print("path in callRestAPI : " + uri + " , method : " + method + ", content type : " +
    //  contentType + ", userId : " + userId + ", userPassword : " + userPassword +
    // " , data : " + data + "\n")


    var httpc = (method: @switch) match {
      case "GET" => Http(addQryParmToUri(uri, data)).header("Content-Type","application/x-www-form-urlencoded").header("Authorization",authToken)
      case "PUT" => Http(uri).put(data).header("content-type", contentType)
      case "DELETE" => Http(uri).method("DELETE")
      case "POST" => Http(uri).postData(data).header("Content-Type", contentType).header("Authorization",authToken)
    }

    val conns = connStr.split(":")
    val connProp = Array(conns(0).toInt, conns(1).toInt)

    httpc = httpc.timeout(connTimeoutMs = connProp(0),
      readTimeoutMs = connProp(1))

    httpc.option(HttpOptions.allowUnsafeSSL)

    if (oauthCredStr == "") {
      httpc = if (userCredStr == "") httpc else {
        val usrCred = userCredStr.split(":")
        httpc.auth(usrCred(0), usrCred(1))
      }
    }
    else {
      val oauthd = oauthCredStr.split(":")
      val consumer = Token(oauthd(0), oauthd(1))
      val accessToken = Token(oauthd(2), oauthd(3))
      httpc.oauth(consumer, accessToken)
    }

    // print("in callRestAPI final http : " + httpc + "\n")

    val resp = (respType : @switch) match {
      case "BODY" => httpc.asString.body
      case "BODY-BYTES" => httpc.asBytes.body
      case "BODY-STREAM" => getBodyStream(httpc)
      case "CODE" => httpc.asString.code
      case "HEADERS" => httpc.asString.headers
      case "LOCATION" => httpc.asString.location.mkString(" ")
    }

    resp
  }

  private def addQryParmToUri(uri: String, data: String) : String = {
    if (uri contains "?") uri + "&" + data else uri + "?" + data
  }

  private def convertToQryParm(data: String) : List[(String, String)] = {
    data.substring(1, data.length - 1).split(",").map(_.split(":"))
      .map{ case Array(k, v) => (k.substring(1, k.length-1), v.substring(1, v.length-1))}
      .toList
  }

  private def getBodyStream(httpReq: scalaj.http.HttpRequest) : InputStream = {

    val conn = (new URL(httpReq.urlBuilder(httpReq))).openConnection.asInstanceOf[HttpURLConnection]

    HttpOptions.method(httpReq.method)(conn)

    httpReq.headers.reverse.foreach{ case (name, value) =>
      conn.setRequestProperty(name, value)
    }

    httpReq.options.reverse.foreach(_(conn))

    httpReq.connectFunc(httpReq, conn)

    conn.getInputStream

  }

  def prepareJsonInput(keys: Array[String], values: Array[String]) : String = {

    val keysLength = keys.length
    var cnt = 0
    val outArrB : ArrayBuffer[String] = new ArrayBuffer[String](keysLength)

    while (cnt < keysLength) {
      if(values(cnt).startsWith("[") || values(cnt).startsWith("{")) //complex datatype (arrays or objects) and it was cast to string
      {
        outArrB += "\"" + keys(cnt) + "\":" + values(cnt)
      }
      else //simple datatype
      {
        outArrB += "\"" + keys(cnt) + "\":\"" + values(cnt) + "\""
      }
      cnt += 1
    }

    "{" + outArrB.mkString(",") + "}"

  }

  def prepareTextInput(keys: Array[String], values: Array[String]) : String = {

    val keysLength = keys.length
    var cnt = 0
    val outArrB : ArrayBuffer[String] = new ArrayBuffer[String](keysLength)

    while (cnt < keysLength) {
      outArrB += URLEncoder.encode(keys(cnt)) + "=" + URLEncoder.encode(values(cnt))
      cnt += 1
    }

    outArrB.mkString("&")

  }

  def prepareJsonOutput(keys: Array[String], values: Array[String], resp: String) : String = {

    val keysLength = keys.length
    var cnt = 0
    val outArrB : ArrayBuffer[String] = new ArrayBuffer[String](keysLength)

    while (cnt < keysLength) {
      if(values(cnt).startsWith("[") || values(cnt).startsWith("{")) //complex datatype (arrays or objects)
      {
        outArrB += "\"" + keys(cnt) + "\":" + values(cnt)
      }
      else //simple datatype
      {
        outArrB += "\"" + keys(cnt) + "\":\"" + values(cnt) + "\""
      }
      cnt += 1
    }

    "{" + outArrB.mkString(",") +  ",\"output\":" + resp + "}"

  }

}
