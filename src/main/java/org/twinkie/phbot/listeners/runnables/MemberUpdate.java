package org.twinkie.phbot.listeners.runnables;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.twinkie.phbot.config.Constants;

import java.awt.*;

public class MemberUpdate {
    public void memberJoinMessage(GuildMemberJoinEvent guildMemberJoinEvent) {
        Guild guild = guildMemberJoinEvent.getGuild();
        User user = guildMemberJoinEvent.getUser();
        TextChannel textChannel = guild.getTextChannelById(Constants.memberJoinChannelId);
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle("Привет "+user.getGlobalName()+"!")
                .setColor(Color.PINK)
                .build();
        MessageCreateData messageCreateData = new MessageCreateBuilder()
                .addContent(user.getAsMention())
                .addEmbeds(messageEmbed)
                .build();
        assert textChannel != null;
        textChannel.sendMessage(messageCreateData).complete();
    }
    public void memberLeaveMessage(GuildMemberRemoveEvent guildMemberRemoveEvent) {
        Guild guild = guildMemberRemoveEvent.getGuild();
        TextChannel textChannel = guild.getTextChannelById(Constants.memberJoinChannelId);
        User user = guildMemberRemoveEvent.getUser();
        MessageCreateData messageCreateData = new MessageCreateBuilder()
                .addContent(user.getGlobalName()+" покидает нас.")
                .build();
        assert textChannel != null;
        textChannel.sendMessage(messageCreateData).complete();
    }
}
