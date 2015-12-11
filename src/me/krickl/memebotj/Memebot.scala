/**
memebotj - an irc bot for twitch.tv
Copyright (c) 2015, Lukas Krickl
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. All advertising materials mentioning features or use of this software
   must display the following acknowledgement:
   This product includes software developed by Lukas Krickl.
4. Neither the name of Lukas Krickl nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Lukas Krickl ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Lukas Krickl BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES
LOSS OF USE, DATA, OR PROFITS OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

**/
package me.krickl.memebotj

import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.{Calendar, Properties}
import java.util.logging.Logger

import org.bson.Document

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase

//import me.krickl.memebotj.api.APIConnectionHandler

/***
 * Memebot is a simpe irc bot for twitch.tv written in Scala
 * @author unlink
 *
 */
object Memebot {
	final var log = Logger.getLogger(Memebot.getClass.getName)

	var ircServer: String = "irc.twitch.tv"
	var ircport: Int = 6667
	var apiport: Int = 6532
	var mongoHost: String = "localhost"
	var mongoPort: Int = 27017
	var apiMasterKey: String = "debug::key"
	var mongoDBName: String = "memebot"
	var home: String = System.getProperty("user.home")
	var memebotDir: String = System.getProperty("user.home") + "/.memebot"
	var htmlDir: String = ""
	var configFile: String = memebotDir + "/memebot.cfg"
	var channelConfig: String = memebotDir + "/channels.cfg"
	var botNick: String = null
	var botPassword: String = null
	var clientID: String = null
	var clientSecret: String = null
	var botAdmins: java.util.List[String] = new java.util.ArrayList[String]()
	var mongoUser: String = ""
	var mongoPassword: String = ""
	var useMongoAuth: Boolean = false
	var pid: Int = 0
	var channels: java.util.ArrayList[String] = new java.util.ArrayList[String]()

	// ConnectionHandler connection = null
	var joinedChannels: java.util.ArrayList[ChannelHandler] = new java.util.ArrayList[ChannelHandler]()
	var youtubeAPIKey: String = ""
	var useMongo: Boolean = true
	// boolean updateToMongo = false
	var mongoClient: MongoClient = null
	var db: MongoDatabase = null

	var lastError: String = ""

	final var messageLimit: Int = 19 // message limit per 30 seconds

	var internalCollection: MongoCollection[Document] = null

	var webBaseURL: String = ""

	var useWeb: Boolean = true

	var isBotMode: Boolean = true


	def main(args: Array[String]) {
		//soon to be used
		for(i <- 0 to args.length) {
		}

		// initial setup
		new File(home + "/.memebot").mkdir()
		new File(home + "/.memebot/channels")

		BuildInfo.loadBuildInfo()

		// read config
		val config = new Properties()
		try {
			config.load(new FileReader(Memebot.configFile))
		} catch {
			case e: FileNotFoundException =>
				try {
					new File(Memebot.configFile).createNewFile()
					// save properties
				} catch {
					case e: IOException =>
						e.printStackTrace()
				}

				e.printStackTrace()
			case e: IOException =>
					e.printStackTrace()
		}

		// read botadmin file
		Memebot.botAdmins.add("#internal#")
		try {
			Memebot.botAdmins = Files.readAllLines(Paths.get(Memebot.memebotDir + "/botadmins.cfg"))
			Memebot.botAdmins.add("#internal#")
		} catch {
			case e3: IOException =>
				e3.printStackTrace()
		}

		Memebot.ircServer = config.getProperty("ircserver", Memebot.ircServer)
		Memebot.ircport = Integer.parseInt(config.getProperty("ircport", Integer.toString(Memebot.ircport)))
		Memebot.mongoHost = config.getProperty("mongohost", Memebot.mongoHost)
		Memebot.mongoPort = Integer.parseInt(config.getProperty("mongoport", Integer.toString(Memebot.mongoPort)))
		Memebot.mongoDBName = config.getProperty("mongodbname", Memebot.mongoDBName)
		Memebot.botNick = config.getProperty("botnick", Memebot.botNick)
		Memebot.botPassword = config.getProperty("botpassword", Memebot.botPassword)
		Memebot.clientID = config.getProperty("clientid", Memebot.clientID)
		Memebot.clientSecret = config.getProperty("clientsecret", Memebot.clientSecret)
		Memebot.htmlDir = config.getProperty("htmldir", Memebot.htmlDir)
		Memebot.youtubeAPIKey = config.getProperty("ytapikey", Memebot.youtubeAPIKey)
		Memebot.mongoUser = config.getProperty("mongouser", Memebot.mongoUser)
		Memebot.mongoPassword = config.getProperty("mongopassword", Memebot.mongoPassword)
		Memebot.useMongoAuth = config.getProperty("mongoauth", Memebot.useMongoAuth.toString).toBoolean
		Memebot.webBaseURL = config.getProperty("weburl", Memebot.webBaseURL)
		Memebot.useWeb = config.getProperty("useweb", Memebot.useWeb.toString).toBoolean
		Memebot.apiMasterKey = config.getOrDefault("apimasterkey", Memebot.apiMasterKey).toString
		Memebot.apiport = config.getOrDefault("apiport", Memebot.apiport.toString).toString.toInt

		if(Memebot.isBotMode) {
			// shutdown hook
			Runtime.getRuntime.addShutdownHook(new Thread() {
				override def run() {
					Memebot.log.warning("Process received SIGTERM...")

					val it = Memebot.joinedChannels.iterator()
					while(it.hasNext) {
						val ch = it.next()
						ch.writeDBChannelData()
						ch.setIsJoined(false)
					}
				}
			})
		}

		Memebot.log.info(f"${BuildInfo.appName} version ${BuildInfo.version} build ${BuildInfo.buildNumber} built on ${BuildInfo.timeStamp}")

		// get pid and write to file
		val f = new File(memebotDir + "/pid")
		var bw: BufferedWriter = null
		try {
			Memebot.log.info("PID: " + ManagementFactory.getRuntimeMXBean.getName)
			bw = new BufferedWriter(new FileWriter(f))
			bw.write(ManagementFactory.getRuntimeMXBean.getName.split("@")(0))
			bw.close()
		} catch {
			case e1: IOException =>
				e1.printStackTrace()
		}

		if(Memebot.isBotMode) {
			// set up database
			if (Memebot.useMongo) {
				if (Memebot.useMongoAuth) {
					val authuri: MongoClientURI = new MongoClientURI(String.format("mongodb://%s:%s@%s/?authSource=%s",
							Memebot.mongoUser, Memebot.mongoPassword, Memebot.mongoHost, Memebot.mongoDBName))
					Memebot.mongoClient = new MongoClient(authuri)
				} else {
					Memebot.mongoClient = new MongoClient(Memebot.mongoHost, Memebot.mongoPort)
				}
				Memebot.db = Memebot.mongoClient.getDatabase(Memebot.mongoDBName)
				Memebot.internalCollection = Memebot.db.getCollection("#internal#")
			}

			try {
				channels = Files.readAllLines(Paths.get(Memebot.channelConfig),
						Charset.defaultCharset()).asInstanceOf[java.util.ArrayList[String]]

			} catch {
				case  e: IOException =>
					e.printStackTrace()
			}

			// setup connection

			// join channels
			val it = Memebot.channels.iterator
			while(it.hasNext) {
				val channel: String = it.next
				Memebot.joinChannel(channel)
			}

			//auto rejoin if a thread crashes
			while(true) {
				for(i <- 0 to Memebot.joinedChannels.size() - 1) {
					val ch: ChannelHandler = Memebot.joinedChannels.get(i)
					if(!ch.getT.isAlive) {
						val channel: String = ch.getChannel
						Memebot.joinedChannels.remove(i)
						Memebot.joinChannel(channel)
					}
				}

				try {
					Thread.sleep(60000)
				} catch {
					case e: InterruptedException =>
						e.printStackTrace()
				}
			}
		}
	}

	def joinChannel(channel: String) {
		try {
			val login: File = new File(Memebot.memebotDir + "/" + channel.replace("\n\r", "") + ".login")
			if (login.exists()) {
				val loginInfo: java.util.ArrayList[String] = Files.readAllLines(Paths.get(Memebot.memebotDir + "/" + channel.replace("\n\r", "") + ".login")).asInstanceOf[java.util.ArrayList[String]]

				Memebot.log.info("Found login file for channel " + channel)

				val newChannel = new ChannelHandler(channel.replace("\n\r", ""), new ConnectionHandler(Memebot.ircServer, Memebot.ircport, loginInfo.get(0), loginInfo.get(1)))
				newChannel.start()
			} else {
				val newChannel = new ChannelHandler(channel.replace("\n\r", ""), new ConnectionHandler(Memebot.ircServer, Memebot.ircport, Memebot.botNick, Memebot.botPassword))
				newChannel.start()
			}
		} catch {
			case e: IOException =>
				e.printStackTrace()
		}
	}

	/***
		* This method formats text for output.
		* The following parameters can be passed in:
		* {sender}
		* {counter}
		* {debugsender}
		* {debugch}
		* {channelweb}
		* {version}
		* {appname}
		* {date}
		* {game}
		* {curremote}
		* {currname}
		* @param fo format object
		* @param channelHandler channelhandler
		* @param sender sender
		* @return
		*/
	def formatText(fo: String, channelHandler: ChannelHandler, sender: UserHandler, commandHandler: CommandHandler): String = {
		val sdfDate = new SimpleDateFormat("yyyy-MM-dd")// dd/MM/yyyy
		val cal = Calendar.getInstance()
		val strDate = sdfDate.format(cal.getTime)

		var formattedOutput = fo

		formattedOutput = formattedOutput.replace("{sender}", sender.screenName)
		formattedOutput = formattedOutput.replace("{counter}", Integer.toString(commandHandler.counter))
		formattedOutput = formattedOutput.replace("{points}", sender.points.toString)
		formattedOutput = formattedOutput.replace("{debugsender}", sender.toString)
		formattedOutput = formattedOutput.replace("{debugch}", commandHandler.toString)
		formattedOutput = formattedOutput.replace("{channelweb}", channelHandler.getChannelPageURL)
		formattedOutput = formattedOutput.replace("{version}", BuildInfo.version)
		formattedOutput = formattedOutput.replace("{developer}", BuildInfo.dev)
		formattedOutput = formattedOutput.replace("{appname}", BuildInfo.appName)
		formattedOutput = formattedOutput.replace("{date}", strDate)
		if(channelHandler.getCurrentGame != null) {
			formattedOutput = formattedOutput.replace("{game}", channelHandler.getCurrentGame)
		}
		formattedOutput = formattedOutput.replace("{curremote}",
			channelHandler.getBuiltInStrings.get("CURRENCY_EMOTE"))
		formattedOutput = formattedOutput.replace("{currname}",
			channelHandler.getBuiltInStrings.get("CURRENCY_NAME"))
		formattedOutput
	}

}
