package me.krickl.memebotj;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Commands.Internal.*;
import me.krickl.memebotj.Connection.IRCConnectionHandler;
import me.krickl.memebotj.Twitch.ChannelAPI;
import me.krickl.memebotj.Utility.Cooldown;
import me.krickl.memebotj.Utility.Localisation;
import me.krickl.memebotj.Utility.MessagePackage;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class ChannelHandler implements Runnable {
    public static Logger log = Logger.getLogger(ChannelHandler.class.getName());

    private String channel = "";
    private IRCConnectionHandler connection = null;
    private String broadcaster = this.channel.replace("#", "");
    private HashMap<String, UserHandler> userList = new java.util.HashMap<String, UserHandler>();
    private Cooldown updateCooldown = new Cooldown(600);
    private ArrayList<CommandHandler> channelCommands = new ArrayList<CommandHandler>();
    private ArrayList<CommandHandler> internalCommands = new ArrayList<CommandHandler>();
    private String followerNotification = "";
    private String raceBaseURL = "http://kadgar.net/live";
    private String greetMessage = "Hello I'm {botnick} {version} the dankest irc bot ever RitzMitz";
    private String currentRaceURL = "";
    private ArrayList<String> fileNameList = new ArrayList<>();

    private short maxFileNameLen = 8;
    private String currentFileName = "";
    private long streamStartTime = 0;

    private String local = "engb";
    private String channelPageBaseURL = Memebot.webBaseURL + this.broadcaster;
    private String htmlDir = Memebot.htmlDir + "/" + this.broadcaster;
    private ArrayList<String> otherLoadedChannels = new java.util.ArrayList<String>();
    private MongoCollection<Document> channelCollection = null;
    private double pointsPerUpdate = 1.0f;
    private Thread t = null;
    private boolean isJoined = true;
    private boolean allowAutogreet = true;
    private boolean isLive = false;
    private int currentMessageCount = 0;
    private Cooldown messageLimitCooldown = new Cooldown(30);
    private Cooldown preventMessageCooldown = new Cooldown(30);
    private String currentGame = "Not Playing";
    private SecureRandom random = new SecureRandom();
    private boolean purgeURLS = false;
    private boolean givePointsWhenOffline = false;

    private int linkTimeout = 1;

    private UserHandler broadcasterHandler = new UserHandler(this.broadcaster, this.channel);
    private File htmlDirF = new File(this.htmlDir);
    private UserHandler readOnlyUser = new UserHandler("#readonly#", this.channel);
    private boolean allowGreetMessage = false;

    private double maxPoints = 100000.0f;

    private double startingPoints = 50.0f;

    private String currencyName = "points";
    private String currencyEmote = "points";
    private String followAnnouncement = "";
    private int maxScreenNameLen = 15;
    private int maxAmountOfNameInList = 25;
    private double pointsTax = 0.0f;
    private ChannelAPI twitchChannelAPI = new ChannelAPI(this);
    private boolean silentMode = false;
    private Localisation localisation = new Localisation(this.local);

    public ChannelHandler(String channel, IRCConnectionHandler connection) {
        this.channel = channel;
        this.connection = connection;
        this.broadcaster = this.channel.replace("#", "");
        broadcasterHandler = new UserHandler(this.broadcaster, this.channel);
        channelPageBaseURL = Memebot.webBaseURL + this.broadcaster;
        htmlDir = Memebot.htmlDir + "/" + this.broadcaster;
        log.info("Joining channel " + this.channel);

        broadcasterHandler.setUserBroadcaster(true);

        broadcasterHandler.setModerator(true);

        this.userList.put(this.broadcaster, broadcasterHandler);

        if (!htmlDirF.exists()) {
            htmlDirF.mkdirs();
        }
        this.joinChannel(this.channel);

        if (Memebot.useMongo) {
            if(!Memebot.channelsPrivate.contains(this.channel)) {
                this.channelCollection = Memebot.db.getCollection(this.channel);
            } else {
                this.channelCollection = Memebot.dbPrivate.getCollection(this.channel);
            }
        }

        this.readDB();
        localisation = new Localisation(this.local);

        // todo add all internal commands
        this.internalCommands.add(new AboutCommand(this, "!about", "#internal#"));
        this.internalCommands.add(new AutogreetCommand(this, "!autogreet", "#internal#"));
        this.internalCommands.add(new EditChannel(this, "!channel", "#internal#"));
        this.internalCommands.add(new HelpCommand(this, "!help", "#internal#"));
        this.internalCommands.add(new HugCommand(this, "!mehug", "#internal#"));
        this.internalCommands.add(new JoinCommand(this, "!mejoin", "#internal#"));
        this.internalCommands.add(new PartCommand(this, "!mepart", "#internal#"));
        this.internalCommands.add(new PointsCommand(this, "!points", "#internal#"));
        this.internalCommands.add(new QuitCommand(this, "!mequit", "#internal#"));
        this.internalCommands.add(new RaceCommand(this, "!race", "#internal#"));
        this.internalCommands.add(new SaveCommand(this, "!mesave", "#internal#"));
        this.internalCommands.add(new WhoisCommand(this, "!whois", "#internal#"));
        this.internalCommands.add(new FilenameCommand(this, "!name", "#internal#"));
        this.internalCommands.add(new EdituserCommand(this, "!user", "#internal#"));
        // todo implement this this. internalCommands.add(new SendMessageCommand(this, "!sm", "#internal#"));
        this.internalCommands.add(new DampeCommand(this, "!dampe", "#internal#"));
        this.internalCommands.add(new DebugCommand(this, "!debug", "#debug#"));
        this.internalCommands.add(new PyramidCommand(this, "!pyramid", "#internal#"));
        this.internalCommands.add(new CommandManager(this, "!command", "#internal#"));
        this.internalCommands.add(new ChannelInfoCommand(this, "!ci", "#internal#"));
        this.internalCommands.add(new BobRossCommand(this, "!bobross", "#internal#"));
        this.internalCommands.add(new RestartThreadCommand(this, "!restartt", "#internal#"));
        // todo implement this this. internalCommands.add(new LotteryCommand(this, "!lottery", "#internal#"));

        CommandHandler issueCommand = new CommandHandler(this, "!issue", "#internal#");
        issueCommand.editCommand("output", "{sender}: Having issues? Write a bugreport at https://github.com/unlink2/memebotj/issues", new UserHandler("#internal#", this.channel));
        this.internalCommands.add(issueCommand);

        CommandHandler annoyingDog = new CommandHandler(this, "AnnoyingZ", "#internal#");
        annoyingDog.setTextTrigger(true);
        annoyingDog.setUnformattedOutput("http://annoying.dog ");
        annoyingDog.setCooldown(new Cooldown(6000));
        this.internalCommands.add(annoyingDog);

        if (this.allowGreetMessage) {
            this.sendMessage(Memebot.formatText(this.greetMessage, this, readOnlyUser, null, false, new String[]{}, ""));
        }
    }

    public void partChannel(String channel) {
        this.connection.sendMessageBytes("PART " + channel + "\n");
        boolean isInList = false;
        ChannelHandler removeThisCH = null;
        for(ChannelHandler ch : Memebot.joinedChannels) {
            if(ch.getChannel().equalsIgnoreCase(channel)) {
                isInList = true;
                removeThisCH = ch;
                break;
            }
        }

        if (isInList && removeThisCH != null) {
            Memebot.joinedChannels.remove(removeThisCH);
            new File(Memebot.home + "/.memebot/channels/" + channel).delete();

        }
        this.sendMessage("Leaving channel :(", this.channel);
        this.isJoined = false;
        this.t.interrupt();
        this.connection.close();
        Memebot.joinedChannels.remove(removeThisCH);
    }

    private void joinChannel(String channel) {
        this.connection.sendMessageBytes("JOIN " + channel + "\n");
        boolean isInList = false;

        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equalsIgnoreCase(channel)){
                isInList = true;
                break;
            }
        }

        if(!isInList) {
            try {
                new File(Memebot.home + "/.memebot/channels/" + channel).createNewFile();
            } catch(IOException e) {
                log.warning(e.toString());
            }
        }
        this.isJoined = true;
    }

    @Override
    public void run() {
        //let update loop run independently
        if (Memebot.useUpdateThread) {
            Thread updateThread = new Thread() {
                @Override
                public void run() {
                    while (isJoined) {
                        update();
                        try {
                            Thread.sleep(100);
                        } catch(InterruptedException e) {

                        }
                    }
                }
            };
            updateThread.start();
        }

        while (this.isJoined) {
            String[] ircmsg = new String[2];
            ircmsg = this.connection.recvData();

            if (this.channel.equalsIgnoreCase(ircmsg[0])) {
                this.handleMessage(ircmsg[1]);
            }
            try {
                Thread.sleep(50);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            if (!Memebot.useUpdateThread) {
                this.update();
            }
        }
    }

    public void update() {
        if (this.messageLimitCooldown.canContinue()) {
            this.messageLimitCooldown.startCooldown();
            this.currentMessageCount = 0;
        }
        if (this.updateCooldown.canContinue()) {
            this.updateCooldown.startCooldown();

            //update channel api information
            twitchChannelAPI.update();

            this.writeDB();
            this.writeHTML();

            ArrayList<String> removeUsers = new java.util.ArrayList<String>();
            Iterator it = this.userList.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                UserHandler uh = this.userList.get(key);
                uh.update(this);
                if (this.isLive || this.givePointsWhenOffline) {
                    uh.setPoints(uh.getPoints() + this.pointsPerUpdate * 10);
                }
                uh.writeDB();

                if (uh.canRemove()) {
                    removeUsers.add(key);
                }
            }
            for (String user : removeUsers) {
                this.userList.remove(user);
            }

            for (CommandHandler ch : this.channelCommands) {
                ch.update();
            }
        }
    }

    public void writeHTML() {

    }

    public void handleMessage(String rawmessage) {
        MessagePackage msgPackage = connection.handleMessage(rawmessage, this);

        if (msgPackage == null) {
            return;
        }

        /*if(msgPackage.messageType.equals("WHISPER" + this.channel)) {
            msgPackage.messageType = "PRIVMSG";
        }*/

        String[] msgContent = msgPackage.messageContent;
        UserHandler sender = msgPackage.sender;
        String msgType = msgPackage.messageType;

        if (!msgType.equals("PRIVMSG")) {
            return;
        }

        String msg = msgContent[0];
        String[] data = java.util.Arrays.copyOfRange(msgContent, 0, msgContent.length);
        if (this.purgeURLS) {
            for (String username : Memebot.globalBanList) {
                if (sender.username.equals(username) && !sender.isModerator) {
                    this.sendMessage("/ban " + sender.username);
                }
            }

            for (String message : Memebot.urlBanList) {
                if (rawmessage.contains(message)) {
                    this.sendMessage("/timeout " + sender.username + " 1");
                }
            }

            for (String message : Memebot.phraseBanList) {
                if (rawmessage.contains(message)) {
                    this.sendMessage("/timeout " + sender.username + " 1");
                }
            }
        }

        //channel commands
        int p = this.findCommand(msg);
        if (p != -1) {
            if (!this.channelCommands.get(p).isTextTrigger()) {
                this.channelCommands.get(p).executeCommand(sender, data);
            }
        }

        //text triggers
        for (String s : msgContent) {
            p = this.findCommand(s);

            if (p != -1) {
                CommandHandler ch = this.channelCommands.get(p);
                if (ch.isTextTrigger()) {
                    ch.executeCommand(sender, new String[]{"", ""});
                }
            }
        }

        //other channel's commands
        for (ChannelHandler ch : Memebot.joinedChannels) {
            for(String och : this.otherLoadedChannels) {
                String channel = ch.getBroadcaster();
                if (ch.getChannel().equals(och) || ch.getBroadcaster().equals(och)) {
                    p = ch.findCommand(msg.replace(och.replace("#", "") + ".", ""));
                    if (p != -1 && msg.contains(channel)) {
                        ch.getChannelCommands().get(p).executeCommand(readOnlyUser, data);
                    }
                }
            }
        }

        //internal text triggers
        for (String s : msgContent) {
            p = this.findCommand(s, this.internalCommands, 0);

            if (p != -1) {
                CommandHandler ch = this.internalCommands.get(p);
                if (ch.isTextTrigger()) {
                    ch.executeCommand(sender, new String[]{"", ""});
                }
            }
        }

        //internal commands
        p = this.findCommand(msg, this.internalCommands, 0);
        if (p != -1) {
            CommandHandler ch = this.internalCommands.get(p);
            if(!ch.isTextTrigger()) {
                ch.executeCommand(sender, java.util.Arrays.copyOfRange(data, 1, data.length));
            }
        }

        //set user activity
        //sender.setTimeSinceActivity(System.currentTimeMillis())
    }

    public void sendMessage(String mesgessage) {
        sendMessage(mesgessage, this.channel, new UserHandler("#internal#", this.channel));
    }

    public void sendMessage(String mesgessage, String channel) {
        sendMessage(mesgessage, channel, new UserHandler("#internal#", this.channel));
    }

    public void sendMessage(String mesgessage, String channel, UserHandler sender) {
        sendMessage(mesgessage, channel, sender, false);
    }

    public void sendMessage(String mesgessage, String channel, UserHandler sender, boolean whisper) {
        String msg = mesgessage;
        if (!this.preventMessageCooldown.canContinue()) {
            return;
        }
        if (this.silentMode) {
            return;
        }
        if (this.currentMessageCount >= me.krickl.memebotj.Memebot.messageLimit) {
            log.warning("Reached global message limit for 30 seconds. try again later");
            this.preventMessageCooldown.startCooldown();
        }
        if(msg.contains("/ignore")) {
            return;
        }

        this.currentMessageCount += 1;
        if(!whisper) {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG " + this.channel + " :" + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG " + this.channel + " : " + msg + "\n");
            }
        } else {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG #jtv :/w " + sender.getUsername() + " " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG #jtv :/w " + sender.getUsername() + " " + msg + "\n");
            }
        }
    }

    public int findCommand(String command) {
        return findCommand(command, this.channelCommands, 0);
    }

    public int findCommand(String command, java.util.ArrayList<CommandHandler> commandList, int i) {
        for (int index = 0; index < commandList.size(); index++) {
            CommandHandler cmd = commandList.get(index);
            if (cmd.getCommandName().equals(command)) {
                return index;
            }

            if (!cmd.isCaseSensitive() && cmd.getCommandName().toLowerCase().equals(command.toLowerCase())) {
                return index;
            }
        }
        return -1;
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, this.channel);
            t.start();
        }
    }

    private void readDB() {
        if (!Memebot.useMongo) {
            return;
        }
        Document channelQuery = new Document("_id", this.channel);
        FindIterable cursor = this.channelCollection.find(channelQuery);
        Document channelData = (Document)cursor.first();
        if (channelData != null) {
            this.maxFileNameLen = (short)channelData.getInteger("maxfilenamelen", this.maxFileNameLen);
            this.raceBaseURL = channelData.getOrDefault("raceurl", this.raceBaseURL).toString();
            this.fileNameList = (java.util.ArrayList<String>)channelData.getOrDefault("fileanmelist", this.fileNameList);
            this.otherLoadedChannels = (java.util.ArrayList<String>)channelData.getOrDefault("otherchannels", this.otherLoadedChannels);
            this.pointsPerUpdate = (double)channelData.getOrDefault("pointsperupdate", this.pointsPerUpdate);
            this.allowAutogreet = (boolean)channelData.getOrDefault("allowautogreet", this.allowAutogreet);
            this.linkTimeout = (int)channelData.getOrDefault("linktimeout", this.linkTimeout);
            this.purgeURLS = (boolean)channelData.getOrDefault("purgelinks", this.purgeURLS);
            this.silentMode = (boolean)channelData.getOrDefault("silent", this.silentMode);
            this.givePointsWhenOffline = (boolean)channelData.getOrDefault("pointswhenoffline", this.givePointsWhenOffline);
            this.allowGreetMessage = (boolean)channelData.getOrDefault("allowgreetmessage", this.givePointsWhenOffline);
            this.maxPoints = (double)channelData.getOrDefault("maxpoints", this.maxPoints);
            this.local = channelData.getOrDefault("local", this.local).toString();
            this.currencyName = channelData.getOrDefault("currname", this.currencyName).toString();
            this.currencyEmote = channelData.getOrDefault("curremote", this.currencyEmote).toString();
            this.followAnnouncement = channelData.getOrDefault("followannouncement", this.followAnnouncement).toString();
            this.maxScreenNameLen = (int)channelData.getOrDefault("maxscreennamelen", this.maxScreenNameLen);
            this.maxAmountOfNameInList = (int)channelData.getOrDefault("maxnameinlist", this.maxScreenNameLen);
            this.pointsTax = (double)channelData.getOrDefault("pointstax", this.pointsTax);
            this.startingPoints = (double)channelData.getOrDefault("startingpoints", this.startingPoints);
        }
        MongoCollection commandCollection = null;
        if(!Memebot.channelsPrivate.contains(this.channel)) {
            commandCollection = Memebot.db.getCollection(this.channel + "_commands");
        } else {
            commandCollection = Memebot.dbPrivate.getCollection(this.channel + "_commands");
        }
        FindIterable comms = commandCollection.find();
        final ChannelHandler ch = this;
        //todo fix this important!
        comms.forEach(new Block<org.bson.Document>(){

            @Override
            public void apply(final Document doc) {
                channelCommands.add(new CommandHandler(ch, doc.getString("command"), ""));
            }
        });
    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }
        ChannelHandler.log.info("Saving data in db for channel " + this.channel + " on DB " + Memebot.db.getName());
        Document channelQuery = new Document("_id", this.channel);

        Document channelData = new Document("_id", this.channel).append("maxfilenamelen", this.maxFileNameLen)
                .append("raceurl", this.raceBaseURL)
                .append("fileanmelist", this.fileNameList)
                .append("otherchannels", this.otherLoadedChannels)
                .append("pointsperupdate", this.pointsPerUpdate)
                .append("allowautogreet", this.allowAutogreet)
                .append("purgelinks", this.purgeURLS)
                .append("linktimeout", this.linkTimeout)
                .append("silent", this.silentMode)
                .append("pointswhenoffline", this.givePointsWhenOffline)
                .append("allowgreetmessage", this.allowGreetMessage)
                .append("maxpoints", this.maxPoints)
                .append("local", this.local)
                .append("currname", this.currencyName)
                .append("curremote", this.currencyEmote)
                .append("followannouncement", this.followAnnouncement)
                .append("maxscreennamelen", this.maxScreenNameLen)
                .append("maxnameinlist", this.maxAmountOfNameInList)
                .append("pointstax", this.pointsTax)
                .append("startingpoints", this.startingPoints);
        try {
            if (this.channelCollection.findOneAndReplace(channelQuery, channelData) == null) {
                this.channelCollection.insertOne(channelData);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        for (String key : this.userList.keySet()) {
            this.userList.get(key).writeDB();
        }
    }



    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        ChannelHandler.log = log;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public IRCConnectionHandler getConnection() {
        return connection;
    }

    public void setConnection(IRCConnectionHandler connection) {
        this.connection = connection;
    }

    public String getBroadcaster() {
        return broadcaster;
    }

    public void setBroadcaster(String broadcaster) {
        this.broadcaster = broadcaster;
    }

    public HashMap<String, UserHandler> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, UserHandler> userList) {
        this.userList = userList;
    }

    public Cooldown getUpdateCooldown() {
        return updateCooldown;
    }

    public void setUpdateCooldown(Cooldown updateCooldown) {
        this.updateCooldown = updateCooldown;
    }

    public ArrayList<CommandHandler> getChannelCommands() {
        return channelCommands;
    }

    public void setChannelCommands(ArrayList<CommandHandler> channelCommands) {
        this.channelCommands = channelCommands;
    }

    public ArrayList<CommandHandler> getInternalCommands() {
        return internalCommands;
    }

    public void setInternalCommands(ArrayList<CommandHandler> internalCommands) {
        this.internalCommands = internalCommands;
    }

    public String getFollowerNotification() {
        return followerNotification;
    }

    public void setFollowerNotification(String followerNotification) {
        this.followerNotification = followerNotification;
    }

    public String getRaceBaseURL() {
        return raceBaseURL;
    }

    public void setRaceBaseURL(String raceBaseURL) {
        this.raceBaseURL = raceBaseURL;
    }

    public String getGreetMessage() {
        return greetMessage;
    }

    public void setGreetMessage(String greetMessage) {
        this.greetMessage = greetMessage;
    }

    public String getCurrentRaceURL() {
        return currentRaceURL;
    }

    public void setCurrentRaceURL(String currentRaceURL) {
        this.currentRaceURL = currentRaceURL;
    }

    public ArrayList<String> getFileNameList() {
        return fileNameList;
    }

    public void setFileNameList(ArrayList<String> fileNameList) {
        this.fileNameList = fileNameList;
    }

    public short getMaxFileNameLen() {
        return maxFileNameLen;
    }

    public void setMaxFileNameLen(short maxFileNameLen) {
        this.maxFileNameLen = maxFileNameLen;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    public long getStreamStartTime() {
        return streamStartTime;
    }

    public void setStreamStartTime(long streamStartTime) {
        this.streamStartTime = streamStartTime;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getChannelPageBaseURL() {
        return channelPageBaseURL;
    }

    public void setChannelPageBaseURL(String channelPageBaseURL) {
        this.channelPageBaseURL = channelPageBaseURL;
    }

    public String getHtmlDir() {
        return htmlDir;
    }

    public void setHtmlDir(String htmlDir) {
        this.htmlDir = htmlDir;
    }

    public ArrayList<String> getOtherLoadedChannels() {
        return otherLoadedChannels;
    }

    public void setOtherLoadedChannels(ArrayList<String> otherLoadedChannels) {
        this.otherLoadedChannels = otherLoadedChannels;
    }

    public MongoCollection<Document> getChannelCollection() {
        return channelCollection;
    }

    public void setChannelCollection(MongoCollection<Document> channelCollection) {
        this.channelCollection = channelCollection;
    }

    public double getPointsPerUpdate() {
        return pointsPerUpdate;
    }

    public void setPointsPerUpdate(double pointsPerUpdate) {
        this.pointsPerUpdate = pointsPerUpdate;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public boolean isAllowAutogreet() {
        return allowAutogreet;
    }

    public void setAllowAutogreet(boolean allowAutogreet) {
        this.allowAutogreet = allowAutogreet;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public int getCurrentMessageCount() {
        return currentMessageCount;
    }

    public void setCurrentMessageCount(int currentMessageCount) {
        this.currentMessageCount = currentMessageCount;
    }

    public Cooldown getMessageLimitCooldown() {
        return messageLimitCooldown;
    }

    public void setMessageLimitCooldown(Cooldown messageLimitCooldown) {
        this.messageLimitCooldown = messageLimitCooldown;
    }

    public Cooldown getPreventMessageCooldown() {
        return preventMessageCooldown;
    }

    public void setPreventMessageCooldown(Cooldown preventMessageCooldown) {
        this.preventMessageCooldown = preventMessageCooldown;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public SecureRandom getRandom() {
        return random;
    }

    public void setRandom(SecureRandom random) {
        this.random = random;
    }

    public boolean isPurgeURLS() {
        return purgeURLS;
    }

    public void setPurgeURLS(boolean purgeURLS) {
        this.purgeURLS = purgeURLS;
    }

    public boolean isGivePointsWhenOffline() {
        return givePointsWhenOffline;
    }

    public void setGivePointsWhenOffline(boolean givePointsWhenOffline) {
        this.givePointsWhenOffline = givePointsWhenOffline;
    }

    public int getLinkTimeout() {
        return linkTimeout;
    }

    public void setLinkTimeout(int linkTimeout) {
        this.linkTimeout = linkTimeout;
    }

    public UserHandler getBroadcasterHandler() {
        return broadcasterHandler;
    }

    public void setBroadcasterHandler(UserHandler broadcasterHandler) {
        this.broadcasterHandler = broadcasterHandler;
    }

    public File getHtmlDirF() {
        return htmlDirF;
    }

    public void setHtmlDirF(File htmlDirF) {
        this.htmlDirF = htmlDirF;
    }

    public UserHandler getReadOnlyUser() {
        return readOnlyUser;
    }

    public void setReadOnlyUser(UserHandler readOnlyUser) {
        this.readOnlyUser = readOnlyUser;
    }

    public boolean isAllowGreetMessage() {
        return allowGreetMessage;
    }

    public void setAllowGreetMessage(boolean allowGreetMessage) {
        this.allowGreetMessage = allowGreetMessage;
    }

    public double getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(double maxPoints) {
        this.maxPoints = maxPoints;
    }

    public double getStartingPoints() {
        return startingPoints;
    }

    public void setStartingPoints(double startingPoints) {
        this.startingPoints = startingPoints;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyEmote() {
        return currencyEmote;
    }

    public void setCurrencyEmote(String currencyEmote) {
        this.currencyEmote = currencyEmote;
    }

    public String getFollowAnnouncement() {
        return followAnnouncement;
    }

    public void setFollowAnnouncement(String followAnnouncement) {
        this.followAnnouncement = followAnnouncement;
    }

    public int getMaxScreenNameLen() {
        return maxScreenNameLen;
    }

    public void setMaxScreenNameLen(int maxScreenNameLen) {
        this.maxScreenNameLen = maxScreenNameLen;
    }

    public int getMaxAmountOfNameInList() {
        return maxAmountOfNameInList;
    }

    public void setMaxAmountOfNameInList(int maxAmountOfNameInList) {
        this.maxAmountOfNameInList = maxAmountOfNameInList;
    }

    public double getPointsTax() {
        return pointsTax;
    }

    public void setPointsTax(double pointsTax) {
        this.pointsTax = pointsTax;
    }

    public ChannelAPI getTwitchChannelAPI() {
        return twitchChannelAPI;
    }

    public void setTwitchChannelAPI(ChannelAPI twitchChannelAPI) {
        this.twitchChannelAPI = twitchChannelAPI;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public Localisation getLocalisation() {
        return localisation;
    }

    public void setLocalisation(Localisation localisation) {
        this.localisation = localisation;
    }
}
