/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twinkie.phbot.library.commandclient.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * A Bot Client interface implemented on objects used to hold bot data.
 *
 * <p>This is implemented in {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}
 * alongside implementation of {@link net.dv8tion.jda.api.hooks.EventListener EventListener} to create a
 * compounded "Client Listener" which catches specific kinds of events thrown by JDA and processes them
 * automatically to handle and execute {@link Command Command}s.
 *
 * <p>Implementations also serve as a useful platforms, carrying reference info such as the bot's
 * {@linkplain #getOwnerId() Owner ID}, {@linkplain #getPrefix() prefix}, and a {@linkplain #getServerInvite()
 * support server invite}.
 *
 * <p>For the CommandClientImpl, once initialized, only the following can be modified:
 * <ul>
 *     <li>{@link Command Command}s may be added or removed.</li>
 *     <li>The {@link CommandListener CommandListener} may be set.</li>
 * </ul>
 *
 * @author John Grosh (jagrosh)
 *
 * @implNote
 *         While typically safe, there are a few ways to misuse the standard implementation of this interface:
 *         the CommandClientImpl.
 *         <br>Because of this the following should <b>ALWAYS</b> be followed to avoid such errors:
 *
 *         <p><b>1)</b> Do not build and add more than one CommandClient to an instance JDA, <b>EVER</b>.
 *
 *         <p><b>2)</b> Always create and add the CommandClientImpl to JDA <b>BEFORE</b> you build it, or there is a
 *                      chance some minor errors will occur, <b>especially</b> if JDA has already fired a {@link
 *                      net.dv8tion.jda.api.events.ReadyEvent ReadyEvent}.
 *
 *         <p><b>3)</b> Do not provide anything other than a String representing a long (and furthermore a User ID) as
 *                      an Owner ID or a CoOwner ID.  This will generate errors, but not stop the creation of the
 *                      CommandClientImpl which will cause several errors to occur very quickly after startup (except
 *                      if you provide {@code null} for the Owner ID, that'll just flat out throw an {@link
 *                      IllegalArgumentException IllegalArgumentException}).
 *
 *         <p><b>4)</b> Do not provide strings when using {@link CommandClientBuilder#setEmojis(String, String, String)
 *                      CommandClientBuilder#setEmojis(String, String, String)} that are not unicode emojis or that do
 *                      not match the custom emote format specified in {@link Emoji#getFormatted()} ()
 *                      Emote#getAsMention()} (IE: {@code <:EmoteName:EmoteID>}).
 *
 *         <p><b>5)</b> Avoid using {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl#linkIds(long,
 *                      net.dv8tion.jda.api.entities.Message)}. This will create errors and has no real purpose outside
 *                      of it's current usage.
 */
public interface CommandClient
{
    /**
     * Gets the Client's prefix.
     *
     * @return A possibly-null prefix
     */
    String getPrefix();

    /**
     * Gets the Client's alternate prefix.
     *
     * @return A possibly-null alternate prefix
     */
    String getAltPrefix();

    /**
     * Gets the array of prefixes
     *
     * @return A possibly-null list of prefixes
     */
    String[] getPrefixes();

    /**
     * Gets the prefix BiConsumer
     *
     * @return A possibly-null prefix BiConsumer
     */
    Function<MessageReceivedEvent, String> getPrefixFunction();

    /**
     * Returns the visual representation of the bot's prefix.
     *
     * <p>This is the same as {@link CommandClient#getPrefix() } unless the prefix is the default,
     * in which case it appears as {@literal @Botname}.
     *
     * @return A never-null prefix
     */
    String getTextualPrefix();

    /**
     * Adds a single {@link Command Command} to this CommandClient's
     * registered Commands.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link Command#name name} and their
     * {@link Command#aliases aliases} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular insertion, the Command provided is inserted at the end of the index, meaning it will
     * become the "rightmost" Command in the ArrayList.
     *
     * @param  command
     *         The Command to add
     *
     * @throws IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered
     */
    void addCommand(Command command);

    /**
     * Adds a single {@link Command Command} to this CommandClient's
     * registered Commands at the specified index.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link Command#name name} and their
     * {@link Command#aliases aliases} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular insertion, the Command provided is inserted at the index specified, meaning it will
     * become the Command located at that index in the ArrayList. This will shift the Command previously located at
     * that index as well as any located at greater indices, right one index ({@code size()+1}).
     *
     * @param  command
     *         The Command to add
     * @param  index
     *         The index to add the Command at (must follow the specifications {@code 0<=index<=size()})
     *
     * @throws ArrayIndexOutOfBoundsException
     *         If {@code index < 0} or {@code index > size()}
     * @throws IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered to an index
     */
    void addCommand(Command command, int index);

    /**
     * Adds a single {@link SlashCommand SlashCommand} to this CommandClient's
     * registered SlashCommand.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link SlashCommand#name name} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of SlashCommands must reorganize the index maintained by the HashMap.
     * <br>For this particular insertion, the SlashCommand provided is inserted at the end of the index, meaning it will
     * become the "rightmost" Command in the ArrayList.
     *
     * @param  command
     *         The Command to add
     *
     * @throws IllegalArgumentException
     *         If the SlashCommand provided has a name or alias that has already been registered
     */
    void addSlashCommand(SlashCommand command);

    /**
     * Adds a single {@link SlashCommand SlashCommand} to this CommandClient's
     * registered Commands at the specified index.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link Command#name name} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular insertion, the Command provided is inserted at the index specified, meaning it will
     * become the Command located at that index in the ArrayList. This will shift the Command previously located at
     * that index as well as any located at greater indices, right one index ({@code size()+1}).
     *
     * @param  command
     *         The Command to add
     * @param  index
     *         The index to add the Command at (must follow the specifications {@code 0<=index<=size()})
     *
     * @throws ArrayIndexOutOfBoundsException
     *         If {@code index < 0} or {@code index > size()}
     * @throws IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered to an index
     */
    void addSlashCommand(SlashCommand command, int index);

    /**
     * Adds a single {@link ContextMenu} to this CommandClient's registered Context Menus.
     *
     * @param  menu
     *         The menu to add
     *
     * @throws IllegalArgumentException
     *         If the Context Menu provided has a name that has already been registered
     */
    void addContextMenu(ContextMenu menu);

    /**
     * Adds a single {@link ContextMenu} to this CommandClient's registered Context Menus.
     *
     * @param  menu
     *         The menu to add
     * @param  index
     *         The index to add the Context Menu at (must follow the specifications {@code 0<=index<=size()})
     *
     * @throws IllegalArgumentException
     *         If the Context Menu provided has a name that has already been registered
     */
    void addContextMenu(ContextMenu menu, int index);

    /**
     * Removes a single {@link Command Command} from this CommandClient's
     * registered Commands at the index linked to the provided name/alias.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link Command#name name} and their
     * {@link Command#aliases aliases} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular removal, the Command removed is that of the corresponding index retrieved by the name
     * provided. This will shift any Commands located at greater indices, left one index ({@code size()-1}).
     *
     * @param  name
     *         The name or an alias of the Command to remove
     *
     * @throws IllegalArgumentException
     *         If the name provided was not registered to an index
     */
    void removeCommand(String name);

    /**
     * Compiles the provided {@link Object Object} annotated with {@link
     * com.jagrosh.jdautilities.command.annotation.JDACommand.Module JDACommand.Module} into a {@link List
     * List} of {@link Command Command}s and adds them to this CommandClient in
     * the order they are listed.
     *
     * <p>This is done through the {@link AnnotatedModuleCompiler
     * AnnotatedModuleCompiler} provided when building this CommandClient.
     *
     * @param  module
     *         An object annotated with JDACommand.Module to compile into commands to be added.
     *
     * @throws IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered
     */
    void addAnnotatedModule(Object module);

    /**
     * Compiles the provided {@link Object Object} annotated with {@link
     * com.jagrosh.jdautilities.command.annotation.JDACommand.Module JDACommand.Module} into a {@link List
     * List} of {@link Command Command}s and adds them to this CommandClient via
     * the {@link Function Function} provided.
     *
     * <p>This is done through the {@link AnnotatedModuleCompiler
     * AnnotatedModuleCompiler} provided when building this CommandClient.
     *
     * <p>The Function will {@link Function#apply(Object) apply} each {@link
     * Command Command} in the compiled list and request an {@code int} in return.
     * <br>Using this {@code int}, the command provided will be applied to the CommandClient via {@link
     * CommandClient#addCommand(Command, int) CommandClient#addCommand(Command, int)}.
     *
     * @param  module
     *         An object annotated with JDACommand.Module to compile into commands to be added.
     * @param  mapFunction
     *         The Function to get indexes for each compiled Command with when adding them to the CommandClient.
     *
     * @throws ArrayIndexOutOfBoundsException
     *         If {@code index < 0} or {@code index > size()}
     * @throws IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered to an index
     */
    void addAnnotatedModule(Object module, Function<Command, Integer> mapFunction);


    /**
     * Sets the {@link CommandListener CommandListener} to catch
     * command-related events thrown by this {@link CommandClient CommandClient}.
     *
     * @param  listener
     *         The CommandListener
     */
    void setListener(CommandListener listener);

    /**
     * Returns the current {@link CommandListener CommandListener}.
     *
     * @return A possibly-null CommandListener
     */
    CommandListener getListener();

    /**
     * Returns the list of registered {@link Command Command}s
     * during this session.
     *
     * @return A never-null List of Commands registered during this session
     */
    List<Command> getCommands();

    /**
     * Returns the list of registered {@link SlashCommand SlashCommand}s
     * during this session.
     *
     * @return A never-null List of Slash Commands registered during this session
     */
    List<SlashCommand> getSlashCommands();

    /**
     * Returns the list of registered {@link ContextMenu}s during this session.
     *
     * @return A never-null List of Context Menus registered during this session
     */
    List<ContextMenu> getContextMenus();

    /**
     * Returns whether manual upsertion is enabled
     *
     * @return The manual upsertion status
     */
    boolean isManualUpsert();

    /**
     * Returns the forced Guild ID for automatic slash command upserts
     *
     * @return A possibly-null forcedGuildId set in the builder
     */
    String forcedGuildId();

    /**
     * Gets the time this {@link CommandClient CommandClient}
     * implementation was created.
     *
     * @return The start time of this CommandClient implementation
     */
    OffsetDateTime getStartTime();

    /**
     * Gets the {@link OffsetDateTime OffsetDateTime} that the specified cooldown expires.
     *
     * @param  name
     *         The cooldown name
     *
     * @return The expiration time, or null if the cooldown does not exist
     */
    OffsetDateTime getCooldown(String name);

    /**
     * Gets the remaining number of seconds on the specified cooldown.
     *
     * @param  name
     *         The cooldown name
     *
     * @return The number of seconds remaining
     */
    int getRemainingCooldown(String name);

    /**
     * Applies the specified cooldown with the provided name.
     *
     * @param  name
     *         The cooldown name
     * @param  seconds
     *         The time to make the cooldown last
     */
    void applyCooldown(String name, int seconds);

    /**
     * Cleans up expired cooldowns to reduce memory.
     */
    void cleanCooldowns();

    /**
     * Gets the number of uses for the provide {@link Command Command}
     * during this session, or {@code 0} if the command is not registered to this CommandClient.
     *
     * @param  command
     *         The Command
     *
     * @return The number of uses for the Command
     */
    int getCommandUses(Command command);

    /**
     * Gets the number of uses for a {@link Command Command}
     * during this session matching the provided String name, or {@code 0} if there is no Command
     * with the name.
     *
     * <p><b>NOTE:</b> this method <b>WILL NOT</b> get uses for a command if an
     * {@link Command#aliases alias} is provided! Also note that
     * {@link Command#children child commands} <b>ARE NOT</b>
     * tracked and providing names or effective names of child commands will return {@code 0}.
     *
     * @param  name
     *         The name of the Command
     *
     * @return The number of uses for the Command, or {@code 0} if the name does not match with a Command
     */
    int getCommandUses(String name);

    /**
     * Gets the ID of the owner of this bot as a String.
     *
     * @return The String ID of the owner of the bot
     */
    String getOwnerId();

    /**
     * Gets the ID of the owner of this bot as a {@code long}.
     *
     * @return The {@code long} ID of the owner of the bot
     */
    long getOwnerIdLong();

    /**
     * Gets the ID(s) of any CoOwners of this bot as a String Array.
     *
     * @return The String ID(s) of any CoOwners of this bot
     */
    String[] getCoOwnerIds();

    /**
     * Gets the ID(s) of any CoOwners of this bot as a {@code long} Array.
     *
     * @return The {@code long} ID(s) of any CoOwners of this bot
     */
    long[] getCoOwnerIdsLong();

    /**
     * Gets the success emoji.
     *
     * @return The success emoji
     */
    String getSuccess();

    /**
     * Gets the warning emoji.
     *
     * @return The warning emoji
     */
    String getWarning();

    /**
     * Gets the error emoji.
     *
     * @return The error emoji
     */
    String getError();

    /**
     * Gets the {@link ScheduledExecutorService ScheduledExecutorService} held by this client.
     *
     * <p>This is used for methods such as {@link CommandEvent#async(Runnable)
     * CommandEvent#async(Runnable)} run code asynchronously.
     *
     * @return The ScheduledExecutorService held by this client.
     */
    ScheduledExecutorService getScheduleExecutor();

    /**
     * Gets the invite to the bot's support server.
     *
     * @return A possibly-null server invite
     */
    String getServerInvite();

    /**
     * Gets an a recently updated count of all the {@link Guild Guild}s
     * the bot is connected to on all shards.
     *
     * <p><b>NOTE:</b> This may not always or should not be assumed accurate! Any time
     * a shard joins or leaves a guild it will update the number retrieved by this method
     * but will not update when other shards join or leave guilds. This means that shards
     * will not always retrieve the same value. For instance:
     * <ul>
     *     <li>1) Shard A joins 10 Guilds</li>
     *     <li>2) Shard B invokes this method</li>
     *     <li>3) Shard A invokes this method</li>
     * </ul>
     * The number retrieved by Shard B will be that of the number retrieved by Shard A,
     * minus 10 guilds because Shard B hasn't updated and accounted for those 10 guilds
     * on Shard A.
     *
     * <p><b>This feature requires a Discord Bots API Key to be set!</b>
     * <br>To set your Discord Bots API Key, you'll have to retrieve it from the
     * <a href="http://bots.discord.pw/">Discord Bots</a> website.
     *
     * @return A recently updated count of all the Guilds the bot is connected to on
     *         all shards.
     */
    int getTotalGuilds();

    /**
     * Gets the word used to invoke a help DM.
     *
     * @return The help word
     */
    String getHelpWord();

    /**
     * Gets whether this CommandClient uses linked deletion.
     *
     * <p>Linking calls is the basic principle of pairing bot responses with their calling
     * {@link net.dv8tion.jda.api.entities.Message Message}s.
     * <br>Using this with a basic function such as deletion, this causes bots to delete their
     * Messages as a response to the calling Message being deleted.
     *
     * @return {@code true} if the bot uses linked deletion, {@code false} otherwise.
     *
     * @see    CommandClientBuilder#setLinkedCacheSize(int)
     *         CommandClientBuilder#setLinkedCacheSize(int)
     */
    boolean usesLinkedDeletion();

    /**
     * Returns an Object of the type parameter that should contain settings relating to the specified
     * {@link Guild Guild}.
     *
     * <p>The returning object for this is specified via provision of a
     * {@link GuildSettingsManager GuildSettingsManager} to
     * {@link CommandClientBuilder#setGuildSettingsManager(GuildSettingsManager)
     * CommandClientBuilder#setGuildSettingsManager(GuildSettingsManager)}, more specifically
     * {@link GuildSettingsManager#getSettings(Guild)
     * GuildSettingsManager#getSettings(Guild)}.
     *
     * @param  <S>
     *         The type of settings the GuildSettingsManager provides
     * @param  guild
     *         The Guild to get Settings for
     *
     * @return The settings object for the Guild, specified in
     *         {@link GuildSettingsManager#getSettings(Guild)
     *         GuildSettingsManager#getSettings(Guild)}, can be {@code null} if the implementation
     *         allows it.
     */
    <S> S getSettingsFor(Guild guild);

    /**
     * Returns the type of {@link GuildSettingsManager GuildSettingsManager},
     * the same type of one provided when building this CommandClient, or {@code null} if one was not provided there.
     *
     * <p>This is good if you want to use non-abstract methods specific to your implementation.
     *
     * @param  <M>
     *         The type of the GuildSettingsManager
     *
     * @return The GuildSettingsManager, or {@code null} if one was not provided when building this CommandClient.
     */
    <M extends GuildSettingsManager> M getSettingsManager();

    /**
     * Shuts down internals of the Command Client, such as the threadpool and guild settings manager
     */
    void shutdown();

    /**
     * Upserts all interactions to the provided {@link #forcedGuildId() forced server}.
     * <br>This runs after the {@link net.dv8tion.jda.api.events.ReadyEvent ReadyEvent} has been fired
     * if {@link #isManualUpsert()} is {@code false}.
     * <br>If {@link #forcedGuildId()} is {@code null}, commands will upsert globally.
     * <b>This may take up to an hour.</b>
     *
     * @param jda The JDA instance to use
     */
    void upsertInteractions(JDA jda);

    /**
     * Upserts all interactions to the provided server.
     * <br>This runs after the {@link net.dv8tion.jda.api.events.ReadyEvent ReadyEvent} has been fired
     * if {@link #isManualUpsert()} is {@code false}.
     * <br>If {@code null} is passed for the server, commands will upsert globally.
     * <b>This may take up to an hour.</b>
     *
     * @param jda The JDA instance to use
     * @param serverId The server to upsert interactions for
     */
    void upsertInteractions(JDA jda, String serverId);
}
