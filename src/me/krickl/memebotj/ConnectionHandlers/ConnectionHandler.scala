package me.krickl.memebotj.ConnectionHandlers

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.Utility.MessagePackage

/**
  * This file is part of memebotj.
  * Created by unlink on 17/02/16.
  */
class ConnectionHandler {
  val server: String = ""
  val botNick: String = ""
  val password: String = ""
  val port: Int = 0

  def ping(): Unit = {

  }

  def recvData(): Array[String] = {
    null
  }

  def handleMessage(rawircmsg: String, channelHandler: ChannelHandler): MessagePackage = {
    null
  }

  def close() = {

  }

  def sendMessage(msg: String): Unit = {

  }

  def sendMessageBytes(msg: String): Unit = {

  }

}