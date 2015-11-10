package me.krickl.memebotj.InternalCommands

import java.util.ArrayList

import me.krickl.memebotj.CommandHandler


class BobRossCommand(channel: String, command: String, dbprefix: String) extends CommandHandler(channel,
  command, dbprefix) {

  this.setListContent(new ArrayList[String]())
  this.setCmdtype("list")
  this.setQuotePrefix("")
  this.setUserCooldownLen(60)
  this.setNeededCooldownBypassPower(75)

  this.listContent.add("There's nothing wrong with having a tree as a friend.")
  this.listContent.add("The secret to doing anything is believing that you can do it. Anything that you believe you can do strong enough, you can do. Anything. As long as you believe.")
  this.listContent.add("We don't make mistakes. We just have happy accidents.")
  this.listContent.add("I think there's an artist hidden at the bottom of every single one of us.")
  this.listContent.add("You too can paint almighty pictures.")
  this.listContent.add("No pressure. Just relax and watch it happen.")
  this.listContent.add("Don’t forget to make all these little things individuals -- all of them special in their own way.")
  this.listContent.add("Find freedom on this canvas.")
  this.listContent.add("It’s so important to do something every day that will make you happy.")
  this.listContent.add("Talent is a pursued interest. Anything that you’re willing to practice, you can do.")
  this.listContent.add("Make love to the canvas.")
  this.listContent.add("[Painting] will bring a lot of good thoughts to your heart.")
  this.listContent.add("We artists are a different breed of people. We're a happy bunch.")
  this.listContent.add("We want happy paintings. Happy paintings. If you want sad things, watch the news.")
  this.listContent.add("That's a crooked tree. We'll send him to Washington.")
  this.listContent.add("Every day is a good day when you paint.")
  this.listContent.add("I think each of us, sometime in our life, has wanted to paint a picture.")
  this.listContent.add("We tell people sometimes: We're like drug dealers, come into town and get everybody absolutely addicted to painting. It doesn't take much to get you addicted.")
  this.listContent.add("They say everything looks better with odd numbers of things. But sometimes I put even numbers -- just to upset the critics.")
  this.listContent.add("Gotta give him a friend. Like I always say, 'Everyone needs a friend.'")
  this.listContent.add("See how it fades right into nothing. That's just what you're looking for.")
  this.listContent.add("If I paint something, I don't want to have to explain what it is.")
  this.listContent.add("Water's like me. It's lazy. Boy, it always looks for the easiest way to do things.")
  this.listContent.add("In painting, you have unlimited power. You have the ability to move mountains. You can bend rivers. But when I get home, the only thing I have power over is the garbage.")
  this.listContent.add("Don’t forget to tell these special people in your life just how special they are to you.")
  this.listContent.add("Didn’t you know you had that much power? You can move mountains. You can do anything.")
  this.listContent.add("I like to beat the brush.")
  this.listContent.add("Just let go -- and fall like a little waterfall.")
  this.listContent.add("Talk to the tree, make friends with it.")
  this.listContent.add("I taught my son to paint mountains like these, and guess what? Now he paints the best darn mountains in the industry.")
  this.listContent.add("I really believe that if you practice enough you could paint the 'Mona Lisa' with a two-inch brush.")
  this.listContent.add("Be so very light. Be a gentle whisper.")
  this.listContent.add("Use absolutely no pressure. Just like an angel’s wing.")
  this.listContent.add("You need the dark in order to show the light.")
  this.listContent.add("You can do anything you want to do. This is your world.")
  this.listContent.add("You have to allow the paint to break to make it beautiful.")
  this.listContent.add("However you think it should be, that’s exactly how it should be.")
  this.listContent.add("In nature, dead trees are just as normal as live trees.")
  this.listContent.add("You can have anything you want in the world -- once you help everyone around you get what they want.")
  this.listContent.add("If you do too much, it’s going to lose its effectiveness.")
  this.listContent.add("This is happy place; little squirrels live here and play.")
  this.listContent.add("That’s where the crows will sit. But we’ll have to put an elevator to put them up there because they can’t fly, but they don’t know that, so they still try.")
  this.listContent.add("Remember how free clouds are. They just lay around in the sky all day long.")
  this.listContent.add("We don’t really know where this goes -- and I’m not sure we really care.")
  this.listContent.add("If we’re going to have animals around we all have to be concerned about them and take care of them.")
  this.listContent.add("You can do anything here -- the only prerequisite is that it makes you happy.")
  this.listContent.add("Go out on a limb -- that’s where the fruit is.")
  this.listContent.add("Isn’t it fantastic that you can change your mind and create all these happy things?")
  this.listContent.add("Anytime you learn, you gain.")
  this.listContent.add("It’s life. It’s interesting. It’s fun.")

}
