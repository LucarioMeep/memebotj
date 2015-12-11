package me.krickl.memebotj.InternalCommands.AdminCommands

import me.krickl.memebotj.{ChannelHandler, CommandHandler, UserHandler}

class PartCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setAccess("botadmin")

	this.setNeededCommandPower(75)

	this.setHelptext("Syntax: !mepart")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		channelHandler.partChannel(this.getChannelOrigin)
	}
}
