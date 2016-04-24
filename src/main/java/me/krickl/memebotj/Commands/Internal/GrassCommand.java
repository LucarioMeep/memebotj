package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Inventory.Item;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

import java.io.File;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * The memes live on
 * This file is part of memebotj.
 * Created by unlink on 22/04/16.
 */
public class GrassCommand extends CommandHandler {
    public GrassCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void initCommand() {

    }

    @Override
    public void overrideDB() {
        this.setCost(5);
        this.setUserCooldownLength(90);
        this.setEnabled(true);
        setWhisper(true);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        SecureRandom random = new SecureRandom();
        ArrayList<String> itemsDirList = Memebot.listDirectory(new File(Memebot.memebotDir + "/items"), 1);

        ArrayList<Item> itemList = new ArrayList<>();

        for(String item : itemsDirList) {
            itemList.add(new Item(item.replace(".cfg", ""), 0));
        }

        int outcome = random.nextInt(10000);

        ArrayList<Item> canDrop = new ArrayList<>();

        for(Item item : itemList) {
            if(outcome <= item.getDropChance()) {
                canDrop.add(item);
            }
        }

        try {
            int drop = random.nextInt(canDrop.size());
            Item droppedItem = canDrop.get(drop);

            getChannelHandler().sendMessage(Memebot.formatText("GRASS_" + droppedItem.getItemname().toUpperCase() + "_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            if(!droppedItem.getItemname().equals("nothing")) {
                sender.getUserInventory().addItem(droppedItem.getItemname(), 1);
            }
        } catch(IllegalArgumentException e) {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_NOTHING_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender, isWhisper());
            e.printStackTrace();
        }
    }
}
