package me.krickl.memebotj.InternalCommands.FunCommands

import me.krickl.memebotj.{Memebot, ChannelHandler, CommandHandler, UserHandler}

 class HugCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel, command, dbprefix) {
	this.setHelptext(Memebot.formatText("HUG_SYNTAX", channelOriginHandler, null, this, true, Array()))


	override protected def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) = {
		try {
		  channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("HUG_SOMEONE"), channelHandler, sender, this, false, Array(sender.screenName, data(0))), this.getChannelOrigin)
		} catch {
		  case e: ArrayIndexOutOfBoundsException => {
			  channelHandler.sendMessage(Memebot.formatText(channelHandler.localisation.localisedStringFor("HUG_NOBODY"), channelHandler, sender, this, false, Array(sender.screenName)), this.getChannelOrigin)
		  }
		}
	}

}
