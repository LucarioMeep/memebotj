package me.krickl.memebotj.InternalCommands

import me.krickl.memebotj.ChannelHandler
import me.krickl.memebotj.CommandHandler
import me.krickl.memebotj.UserHandler

class PointsCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setHelptext("Shows points of user")

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		this.setSuccess(false)
		if (channelHandler.getUserList.containsKey(sender.getUsername.toLowerCase())) {
			if (data.length < 1) {
				channelHandler.sendMessage(f"${sender.getUsername}: ${channelHandler.getUserList.get(sender.getUsername).points} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}", this.getChannelOrigin)
			} else {
				try {
					var target: UserHandler = null
					if (channelHandler.getUserList.containsKey(data(1).toLowerCase())) {
						target = channelHandler.getUserList.get(data(1).toLowerCase())
					} else {
						target = new UserHandler(data(1), this.getChannelOrigin)
						if (target.newUser) {
							target = null
						}
					}
					if (target != null &&
							CommandHandler.checkPermission(sender.getUsername, this.getNeededBotAdminCommandPower, channelHandler.getUserList)) {
						val number = java.lang.Double.parseDouble(data(2))
						if (data(0) == "add") {
							target.setPoints(target.points + number)
							this.setSuccess(true)
						} else if (data(0) == "sub") {
							target.setPoints(target.points - number)
							this.setSuccess(true)
						} else if (data(0) == "set") {
							target.setPoints(number)
							this.setSuccess(true)
						}
						if (this.getSuccess) {
							channelHandler.sendMessage(f"${target.getUsername} your new total is: ${target.points} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}", this.getChannelOrigin)
						}
					}
					if (target != null && !this.getSuccess) {
						val number = java.lang.Double.parseDouble(data(2))
						val tax = number / 100 * 10
						if (data(0) == "send") {
							if (this.checkCost(sender, number + tax, channelHandler)) {
								sender.setPoints(sender.points - (number + tax))
								target.setPoints(target.points + number)
								channelHandler.sendMessage(f"${sender.getUsername}: You sent $number ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")} to ${target.getUsername}", this.getChannelOrigin)
							} else {
								channelHandler.sendMessage(f"${sender.getUsername}: Sorry you don't have ${number + tax} ${channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE")}", this.getChannelOrigin)
							}
						}
					}
				} catch {
					case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
				}
			}
		}
	}
}
