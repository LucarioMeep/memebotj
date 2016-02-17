package me.krickl.memebotj.InternalCommands.UserCommands

import java.util.Random

import me.krickl.memebotj._
import me.krickl.memebotj.Utility.CommandPower

//remove if not needed
import scala.collection.JavaConversions._


class FilenameCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setHelptext("Syntax: !name <filename> || !name get || !name current")

  //this.setListregex("/^[一-龠ぁ-ゔァ-ヴーa-zA-Z0-9_,.-々〆〤]{1,8}$/u")

  this.setPointCost(0)

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    try {

      // count how many names a user has in the list
      var amountOfNamesInList = 0

      for(name <- channelHandler.getFileNameList) {
        if(name.contains(f"${data(0)}#${sender.username}")) {
          amountOfNamesInList = amountOfNamesInList + 1
        }
      }

      val cost = channelHandler.pointsPerUpdate * 40

      if(data.length < 2) {
        if (data(0) == "get") {
          if (CommandHandler.checkPermission(sender, CommandPower.broadcasterAbsolute, channelHandler.getUserList)) {
            var amount = 1

            try {
              amount = data(1).toInt
            } catch {
              case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
            }

            for (i <- 0 until amount) {
              val rand = new Random()
              val index = rand.nextInt(channelHandler.getFileNameList.size - 1)
              channelHandler.setCurrentFileName(channelHandler.getFileNameList.get(index))
              channelHandler.getFileNameList.remove(index)
              channelHandler.sendMessage(Memebot.formatText("NAME_PICK", channelHandler, sender, this, true, Array(channelHandler.getCurrentFileName.split("#")(0), channelHandler.getCurrentFileName.split("#")(1))), this.getChannelOrigin)
            }
            return
          } else {
            channelHandler.sendMessage(Memebot.formatText("NAME_GET_ERROR", channelHandler, sender, this, true, Array(), this.getChannelOrigin))
            return
          }
        } else if (data(0) == "current") {
          channelHandler.sendMessage(Memebot.formatText("NAME_PICK", channelHandler, sender, this, true, Array(channelHandler.getCurrentFileName.split("#")(0), channelHandler.getCurrentFileName.split("#")(1))), this.getChannelOrigin)
          return
        } else if (data(0) == "list") {
          channelHandler.sendMessage(s"${channelHandler.getChannelPageBaseURL}/filenames.html", this.channelOrigin)
          return
        } else if (data(0) == "return" && CommandHandler.checkPermission(sender, CommandPower.broadcasterAbsolute, channelHandler.getUserList)) {
          channelHandler.getFileNameList.add(channelHandler.getCurrentFileName)
          channelHandler.sendMessage(Memebot.formatText("NAME_RETURN", channelHandler, sender, this, true, Array(f"$counter")), this.getChannelOrigin)
          return
        } else if (data(0) == "remove" &&
          CommandHandler.checkPermission(sender, CommandPower.broadcasterAbsolute, channelHandler.getUserList)) {
          var counter = 0
          var i = channelHandler.getFileNameList.size - 1
          while (i >= 0) {
            val name = channelHandler.getFileNameList.get(i).split("#")(0)
            if (data(1) == name) {
              channelHandler.getFileNameList.remove(i)
              counter += 1
            }
            i -= 1
          }
          channelHandler.sendMessage(Memebot.formatText("NAME_REMOVE", channelHandler, sender, this, true, Array(f"$counter")), this.getChannelOrigin)
          return
        } else if (data(0) == "count") {
          channelHandler.sendMessage(Memebot.formatText("NAME_COUNT", channelHandler, sender, this, true, Array(f"${channelHandler.fileNameList.size()}")), this.getChannelOrigin)
          return
        } else if (data(0) == "cost") {
          channelHandler.sendMessage(Memebot.formatText("NAME_COST", channelHandler, sender, this, true, Array(f"${cost}")), this.getChannelOrigin)
          return
        }
      }

      var i: Int = 1
      if(data.length >= 2) {
        try {
          i = java.lang.Integer.parseInt(data(1))
        } catch {
          case e@(_: ArrayIndexOutOfBoundsException | _: NumberFormatException) =>
            channelHandler.sendMessage(this.helptext, this.channelOrigin)
            return
        }
      }
      var success = false
      if (data(0).length  <= channelHandler.getMaxFileNameLen) {
        if(amountOfNamesInList + i > channelHandler.maxAmountOfNameInList) {
          channelHandler.sendMessage(Memebot.formatText("NAME_ADD_TOO_MANY", channelHandler, sender, this, true, Array(f"${channelHandler.maxAmountOfNameInList}")), this.getChannelOrigin)
          success = false
        }
        else if (!this.checkCost(sender, cost * i + channelHandler.pointsPerUpdate, channelHandler)) {
          channelHandler.sendMessage(Memebot.formatText("NAME_NOT_ENOUGH_MONEY", channelHandler, sender, this, true, Array(f"${cost * i}")), this.getChannelOrigin)
        } else {
          if(i > 1) {
            channelHandler.sendMessage(Memebot.formatText("NAME_ADD_MANY", channelHandler, sender, this, true, Array(data(0), data(1))), this.getChannelOrigin)
          }
          else {
              channelHandler.sendMessage(Memebot.formatText("NAME_ADD", channelHandler, sender, this, true, Array(data(0))), this.getChannelOrigin)
            }
          sender.setPoints(sender.points - (cost * i))
          success = true
        }
      } else {
        channelHandler.sendMessage(Memebot.formatText("NAME_ADD_TOO_LONG", channelHandler, sender, this, true, Array(f"${channelHandler.maxFileNameLen}")), this.getChannelOrigin)
      }
      for (c <- 0 until i if success) {
        channelHandler.getFileNameList.add(data(0).replace("#", "") + "#" + sender.getUsername)
      }
      channelHandler.writeDBChannelData()
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        channelHandler.sendMessage(this.helptext, this.getChannelOrigin)
        e.printStackTrace()
      case e: java.lang.IllegalArgumentException =>
        e.printStackTrace()
        channelHandler.sendMessage(Memebot.formatText("NAME_EMPTY", channelHandler, sender, this, true, Array()))
      case e: NumberFormatException => e.printStackTrace()
    }
  }
}
