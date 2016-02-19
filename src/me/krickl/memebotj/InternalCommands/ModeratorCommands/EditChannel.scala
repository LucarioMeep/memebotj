package me.krickl.memebotj.InternalCommands.ModeratorCommands

import me.krickl.memebotj.Utility.{CommandPower, Localisation}
import me.krickl.memebotj._

//remove if not needed

class EditChannel(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setNeededCommandPower(CommandPower.broadcasterAbsolute)

  this.setHelptext("")

  override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
    try {
      var newEntry = ""
      for (x <- 1 until data.length) {
        newEntry = newEntry + " " + data(x)
      }

      if (data(0) == "race") {
        channelHandler.setRaceBaseURL(data(1))
      } else if (data(0) == "otherchannel") {
        if (data(1) == "add") {
          channelHandler.getOtherLoadedChannels.add(data(2).toLowerCase)
        } else if (data(1) == "remove") {
          var index = -1
          for (x <- 0 until channelHandler.getOtherLoadedChannels.size) {
            if (channelHandler.getOtherLoadedChannels.get(x).toLowerCase == data(2).toLowerCase) {
              index = x
            }
          }
          if (index >= 0) {
            channelHandler.getOtherLoadedChannels.remove(index)
          }
        } else if (data(1) == "clear") {
          channelHandler.getOtherLoadedChannels.clear()
        }
      } else if (data(0) == "allowautogreet") {
        channelHandler.setAllowAutogreet(!channelHandler.isAllowAutogreet)
        channelHandler.sendMessage(String.format("Autogreet set to %s", java.lang.Boolean.toString(channelHandler.isAllowAutogreet)),
          this.getChannelOrigin)
      } else if (data(0) == "maxnamelen") {
        channelHandler.setMaxFileNameLen(java.lang.Integer.parseInt(data(1)))
        channelHandler.sendMessage("Changed max filename length to " + data(1), this.getChannelOrigin)
      } else if (data(0) == "ppi") {
        channelHandler.setPointsPerUpdate(java.lang.Double.parseDouble(data(1)))
        channelHandler.sendMessage("Changed max ppi to " + data(1), this.getChannelOrigin)
      } else if (data(0) == "purgelinks") {
        channelHandler.setPurgeURLS(java.lang.Boolean.parseBoolean(data(1)))
      } else if (data(0) == "linkto") {
        channelHandler.setLinkTimeout(java.lang.Integer.parseInt(data(1)))
      } else if (data(0) == "silent") {
        channelHandler.setSilentMode(java.lang.Boolean.parseBoolean(data(1)))
      } else if (data(0) == "preventspam") {
        channelHandler.setSpamPrevention(java.lang.Boolean.parseBoolean(data(1)))
      } else if (data(0) == "spamtimeout") {
        channelHandler.setSpamTimeout(java.lang.Integer.parseInt(data(1)))
      } else if (data(0) == "allowgreet") {
        channelHandler.allowGreetMessage = data(1).toBoolean
      } else if (data(0) == "maxpoints") {
        channelHandler.maxPoints = data(1).toDouble
      } else if (data(0) == "local") {
        channelHandler.local = data(1).toString
        channelHandler.localisation = new Localisation(channelHandler.local)
      } else if (data(0) == "curremote") {
        channelHandler.currencyEmote = data(1)
      } else if (data(0) == "currname") {
        channelHandler.currencyName = data(1)
      } else if (data(0) == "followannouncement") {
        if (newEntry != "{none}") {
          channelHandler.followAnnouncement = newEntry
        } else {
          channelHandler.followAnnouncement = ""
        }
      } else if (data(0) == "tax") {
        channelHandler.pointsTax = data(1).asInstanceOf[Double]
      } else if (data(0) == "maxname") {
        channelHandler.maxAmountOfNameInList = data(1).asInstanceOf[Int]
      }
      channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("EDIT_CHANNEL_OK"), channelHandler, sender, this, false, Array(sender.getUsername, data(0), data(1))), this.getChannelOrigin)
      channelHandler.writeDBChannelData()
    } catch {
      case e: ArrayIndexOutOfBoundsException => channelHandler.sendMessage(Memebot.formatText("CHCHANNEL_SYNTAX", channelHandler, sender, this, true),
        this.getChannelOrigin)
      case e: NumberFormatException => e.printStackTrace()
    }
  }
}
