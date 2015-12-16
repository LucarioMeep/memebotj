package me.krickl.memebotj.InternalCommands.UserCommands

import me.krickl.memebotj.{Memebot, ChannelHandler, CommandHandler, UserHandler}

class PointsCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
	command, dbprefix) {

	this.setHelptext("Syntax: !points || !points send <target> <amount>")
	this.setUserCooldownLen(40)

	override def commandScript(sender: UserHandler, channelHandler: ChannelHandler, data: Array[String]) {
		this.setSuccess(false)
		if (channelHandler.getUserList.containsKey(sender.getUsername.toLowerCase())) {
			if (data.length < 1) {
				channelHandler.sendMessage(f"${sender.screenName}: ${"%.2f".format(channelHandler.getUserList.get(sender.getUsername).points)} ${channelHandler.currencyEmote}", this.getChannelOrigin)
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
							channelHandler.sendMessage(Memebot.formatText("POINTS_MOD_NEW_TOTAL", channelHandler, sender, this, true, Array(target.screenName, "%.2f".format(target.points))), this.getChannelOrigin)
						}
					}
					if (target != null && !this.getSuccess) {
						val number = java.lang.Double.parseDouble(data(2))
						if(number < 1) {
							return
						}
						val tax = number / 100 * 10
						if (data(0) == "send") {
							if (this.checkCost(sender, number + tax, channelHandler)) {
								sender.setPoints(sender.points - (number + tax))
								target.setPoints(target.points + number)
								channelHandler.sendMessage(Memebot.formatText("POINTS_SEND", channelHandler, sender, this, true, Array("%.2f".format(number),target.screenName, "%.2f".format(tax))), this.getChannelOrigin)
							} else {
                channelHandler.sendMessage(Memebot.formatText("POINTS_SEND_FAIL", channelHandler, sender, this, true, Array("%.2f".format(number + tax))), this.getChannelOrigin)
              }
						}
					}
				} catch {
					case e: ArrayIndexOutOfBoundsException => e.printStackTrace()
          case e: NumberFormatException => e.printStackTrace()
				}
			}
		}
	}
}
