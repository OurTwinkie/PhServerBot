package org.twinkie.phbot;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.twinkie.phbot.config.Constants;
import org.twinkie.phbot.library.commandclient.command.Command;
import org.twinkie.phbot.library.commandclient.command.CommandClient;
import org.twinkie.phbot.library.commandclient.command.CommandClientBuilder;

import java.util.Arrays;
import java.util.TimeZone;

public class Main {

    private static MongoClient mongoClient;
    private static void createMongoClientConnection(){
        mongoClient = new MongoClient(new MongoClientURI(Constants.mongoDatabaseLink));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        createMongoClientConnection();
        CommandClient commandClient = new CommandClientBuilder()
                .useHelpBuilder(true)
                .useDefaultGame()
                .forceGuildOnly(Constants.guildId)
                .setPrefix(Constants.PREFIX)
                .setOwnerId(Constants.ownerId)
                .setHelpConsumer(commandEvent -> {
                    StringBuilder builder = new StringBuilder("**"+commandEvent.getSelfUser().getName()+"** команды:\n");
                    for(Command command : commandEvent.getClient().getCommands())
                    {
                        if(!command.isHidden() && (!command.isOwnerCommand() || commandEvent.isOwner()))
                        {

                            builder.append("\n`").append(Constants.PREFIX).append(command.getName())
                                    .append(command.getArguments()==null ? "`" : " "+command.getArguments()+"`")
                                    .append(" - ").append(command.getHelp());
                        }
                    }
                    commandEvent.reply(builder.toString());
                })
                .build();
        JDA jda = JDABuilder
                .create(Constants.discordToken, Arrays.asList(Constants.INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(commandClient)
                .build();

    }
}