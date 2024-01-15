package org.twinkie.phbot.config;

import net.dv8tion.jda.api.requests.GatewayIntent;

public class Constants {
    public static final String PREFIX = "!";
    public static final long guildId = 0L;
    public static final long ownerId = 0L;
    public static final GatewayIntent[] INTENTS = {
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
    };
    public static final String mongoDatabaseLink = "";
    public static final String discordToken = "";

}
