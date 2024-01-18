package org.twinkie.phbot.commands.ModerationCommands;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.bson.Document;
import org.twinkie.phbot.commands.commandscategory.ModerationCategory;
import org.twinkie.phbot.config.Emoji;
import org.twinkie.phbot.library.commandclient.command.CommandClient;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;
import org.twinkie.phbot.library.commandclient.commons.utils.FinderUtil;
import org.twinkie.phbot.library.commandclient.commons.waiter.EventWaiter;
import org.twinkie.phbot.utils.formatutils.FormatUtil;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ModerationCmd extends ModerationCategory {
    private final EventWaiter waiter;
    private final MongoDatabase database;
    public ModerationCmd(MongoDatabase database, EventWaiter waiter) {
        this.waiter = waiter;
        this.database = database;
        this.name = "moderation";
        this.guildOnly = true;
        this.help = "использовать возможности модерации";
        this.arguments = "пользователь";
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        List<Member> memberList = FinderUtil.findMembers(args, guild);
        if (memberList.isEmpty()) {
            event.replyError(author.getAsMention() + " Вы не указали пользователя.");
            return;
        }
        Member mentionedMember = memberList.get(0);
        if (mentionedMember == null) {
            event.replyError("Пользователь не найден.");
            return;
        }
        if (mentionedMember.getUser().isBot() || mentionedMember.getUser().isSystem()) {
            event.replyError("Вы не можете применять команды модерации к этому пользователю.");
            return;
        }
        if (mentionedMember.equals(event.getMember())) {
            event.replyError("Вы не можете применять команды модерации к себе.");
            return;
        }


        if (!Objects.requireNonNull(event.getMember())
                .canInteract(mentionedMember)) {
            event.replyError("Вы не можете применять команды модерации к этому пользователю.");
            return;
        }


        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.WHITE)
                .setTitle("Модерирование " + FormatUtil.formatUser(mentionedMember.getUser()));

        StringSelectMenu selectMenu = StringSelectMenu.create("moderationMenu")
                .addOption("Информация о участнике", "info")
                .addOption("Выдать локал-бан","localBan")
                .addOption("Забрать локал-бан", "unLocalBan")
                .addOption("Забанить", "ban")
                .addOption("Разбанить", "unBan")
                .addOption("Кикнуть", "kick")
                .addOption("Замьютить", "mute")
                .addOption("Размьютить", "unMute")
                .addOption("Выдать голосовой мьют", "voiceMute")
                .addOption("Снять голосовой мьют", "voiceUnMute")
                .addOption("Выдать варн", "warn")
                .addOption("Снять варн", "unWarn")
                .setRequiredRange(1, 1)
                .setPlaceholder("Меню модерации")
                .build();

        MessageCreateBuilder messageEditBuilder = new MessageCreateBuilder()
                .setEmbeds(builder.build())
                .setActionRow(selectMenu);

        AtomicReference<Message> message = new AtomicReference<>();
        event.reply(messageEditBuilder.build(), message1 -> message.set(message1));

        waiter.waitForEvent(StringSelectInteractionEvent.class,
                selectMenuInteractionEvent -> checkMenuMember(selectMenuInteractionEvent, event.getMember(), message.get(), event.getClient()),
                selectMenuInteractionEvent ->
                {
                    messageEditBuilder.setActionRow(selectMenu.asDisabled());
                    selectMenuInteractionEvent.getMessage().editMessage(MessageEditData.fromCreateData(messageEditBuilder.build())).complete();
                    String value = selectMenuInteractionEvent.getSelectedOptions().get(0).getValue();
                    if (value.equals("ban"))
                    {
                        ban(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("unBan"))
                    {
                        unban(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("kick"))
                    {
                        kick(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("mute"))
                    {
                        textMute(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("unMute"))
                    {
                        textUnMute(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("warn"))
                    {
                        warn(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("unWarn"))
                    {
                        unWarn(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient());
                        return;
                    }
                    if (value.equals("info")) {
                        getInfo(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent);
                        return;
                    }
                },
                2,
                TimeUnit.MINUTES,
                () ->
                {
                    message.get().editMessageComponents(ActionRow.of(selectMenu.asDisabled())).complete();
                });

    }

    private void getInfo(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent) {
        if (member.hasPermission(Permission.MODERATE_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на просмотр информации о пользователе.").complete();
            return;
        }

        Member menMember = member.getGuild().getMemberById(mentionedMember.getId());

        StringBuilder stringBuilder = new StringBuilder();

        if (menMember == null)
            stringBuilder.append("Пользователь не находится на сервере.").append("\n");
        else
            stringBuilder.append("Пользователь находится на сервере.").append("\n");

        stringBuilder.append("Количество варнов: `")
                .append(getWarnCount(mentionedMember))
                .append("`")
                .append("\n");
        long time = getTimer("mute", mentionedMember);
        if (time != -1) {
            stringBuilder.append("Время мьюта: `")
                    .append(FormatUtil.formatTime(time * 60000L))
                    .append("`")
                    .append("\n");
        }


        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Информация о " + FormatUtil.formatUser(mentionedMember))
                .setColor(selectMenuInteractionEvent.getGuild()
                        .getSelfMember()
                        .getColor())
                .setDescription(stringBuilder.toString());

        selectMenuInteractionEvent.reply(MessageCreateData.fromEmbeds(embedBuilder.build())).complete();
    }

    private void warn(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.MODERATE_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на выдачу варнов пользователям.").complete();
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder()
                .addContent(client.getSuccess() + " Пользователю " + FormatUtil.formatUser(mentionedMember) + " был выдан варн, текущее количество варнов у пользователя: `" + addWarn(mentionedMember) + "`.");

        selectMenuInteractionEvent.reply(messageCreateBuilder.build()).complete();

        if (getWarnCount(mentionedMember) >= 3) {
            member.getGuild().ban(mentionedMember,0, TimeUnit.SECONDS).reason("3 warns").complete();
            return;
        }
    }

    private void unWarn(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.MODERATE_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на выдачу варнов пользователям.").complete();
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder()
                .addContent(client.getSuccess() + " У пользователя " + FormatUtil.formatUser(mentionedMember) + " был снят варн, текущее количество варнов у пользователя: `" + removeWarn(mentionedMember) + "`.");

        selectMenuInteractionEvent.reply(messageCreateBuilder.build()).complete();
    }

    private void textUnMute(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.MODERATE_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на мьют пользователя.").complete();
            return;
        }
        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        if (!mentioned.isTimedOut()) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У пользователя нет мьюта.").complete();
            return;
        }

        TextInput reason = TextInput.create("windowUnMute", "Причина размьюта:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину размьюта здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();


        Modal modal = Modal.create("unMuteModal", "Дополнительный ввод")
                .addActionRow(reason)
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowUnMute"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был размьючен по причине: " + answer).complete();
                        member.getGuild().removeTimeout(mentionedMember).reason(answer  + " UnMute by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был размьючен.").complete();
                        member.getGuild().removeTimeout(mentionedMember).reason("UnMute by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }

                },5,TimeUnit.MINUTES,() -> {});

    }

    private void textMute(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.MODERATE_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на мьют пользователя.").complete();
            return;
        }

        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        if (mentioned.isTimedOut()) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "Пользователь уже получил мьют.").complete();
            return;
        }

        TextInput reason = TextInput.create("windowMute", "Причина мьюта:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину мьюта здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();

        TextInput time = TextInput.create("windowMuteTime", "Время мьюта:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите время мьюта здесь (d - дни, h - часы, m - минуты)")
                .setMinLength(1)
                .setMaxLength(30)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("muteModal", "Дополнительный ввод")
                .addActionRows(ActionRow.of(reason), ActionRow.of(time))
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowMute"))
                            .getAsString();

                    String timeString = Objects.requireNonNull(modalInteractionEvent.getValue("windowMuteTime"))
                            .getAsString();

                    if (timeString.length() != 0) {

                        String[] args = timeString.split("\\s+");

                        AtomicInteger atomicInteger = new AtomicInteger();
                        AtomicBoolean atomicBoolean = new AtomicBoolean();
                        for (String arg : args) {
                            int argsL = arg.length();
                            char[] dst = new char[(argsL - 1)];
                            arg.getChars(0, argsL - 1, dst, 0);
                            char timeunit = arg.charAt(argsL - 1);
                            switch (String.valueOf(timeunit)
                                    .toLowerCase()) {
                                case "h":
                                    atomicInteger.getAndAdd(Integer.parseInt(String.valueOf(dst)) * 60);
                                    atomicBoolean.set(true);
                                    break;
                                case "m":
                                    atomicInteger.getAndAdd(Integer.parseInt(String.valueOf(dst)));
                                    atomicBoolean.set(true);
                                    break;
                                case "d":
                                    atomicInteger.getAndAdd(Integer.parseInt(String.valueOf(dst)) * 1440);
                                    atomicBoolean.set(true);
                                    break;
                            }
                        }
                        if (atomicBoolean.get()) {
                            if (answer.length() != 0) {
                                modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был замьючен по причине: " + answer + " `На время: " + FormatUtil.formatTime(atomicInteger.get()*60000L) + "`").complete();
                                member.getGuild().timeoutFor(mentionedMember, atomicInteger.get(), TimeUnit.MINUTES).reason(answer  + " Mute by: " + FormatUtil.formatUser(member.getUser()) + " Time: " + FormatUtil.formatTime(atomicInteger.get()*60000L)).complete();
                            }
                            else {
                                modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был замьючен." + " `На время: " + FormatUtil.formatTime(atomicInteger.get()*60000L) + "`").complete();
                                member.getGuild().timeoutFor(mentionedMember, atomicInteger.get(), TimeUnit.MINUTES).reason("Mute by: " + FormatUtil.formatUser(member.getUser()) + " Time: " + FormatUtil.formatTime(atomicInteger.get()*60000L)).complete();
                            }
                        }
                        else {
                            modalInteractionEvent.reply(Emoji.ERROR + "Время не может быть определено, проверьте правильность ввода.").complete();
                        }
                    }

                },5,TimeUnit.MINUTES,() -> {});

    }

    private void kick(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.KICK_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на кик пользователей.").complete();
            return;
        }

        TextInput body = TextInput.create("windowKick", "Причина кика:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину кика здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("kickModal", "Дополнительный ввод")
                .addActionRow(body)
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowKick"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был выгнан по причине: " + answer).complete();
                        member.getGuild().kick(mentionedMember).reason(answer  + " Kick by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был выгнан.").complete();
                        member.getGuild().kick(mentionedMember).reason("Kick by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                },5,TimeUnit.MINUTES,() -> {});

    }

    private void unban(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.BAN_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на разбан пользователей.").complete();
            return;
        }

        if (!checkBan(mentionedMember, member.getGuild())) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "Пользователь не находится в бане.").complete();
            return;
        }

        TextInput body = TextInput.create("windowUnBan", "Причина разблокировки:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину разблокировки здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("unbanModal", "Дополнительный ввод")
                .addActionRow(body)
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowUnBan"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был разбанен по причине: " + answer).complete();
                        member.getGuild().unban(mentionedMember).reason(answer  + " Ban by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был разбанен.").complete();
                        member.getGuild().unban(mentionedMember).reason("Ban by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                },5,TimeUnit.MINUTES,() -> {});

    }

    private void ban(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client) {
        if (member.hasPermission(Permission.BAN_MEMBERS) && member.canInteract(Objects.requireNonNull(Objects.requireNonNull(selectMenuInteractionEvent.getGuild()).getMember(mentionedMember)))) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "У вас нет прав на бан пользователей.").complete();
            return;
        }

        if (checkBan(mentionedMember, member.getGuild())) {
            selectMenuInteractionEvent.reply(Emoji.ERROR + "Пользователь уже находится в бане.").complete();
            return;
        }

        TextInput body = TextInput.create("windowBan", "Причина блокировки:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину блокировки здесь")
                .setMinLength(1)
                .setMaxLength(500)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("banModal", "Дополнительный ввод")
                .addActionRow(body)
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowBan"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был забанен по причине: " + answer).complete();
                        member.getGuild().ban(mentionedMember, 0, TimeUnit.SECONDS).reason(answer).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был забанен.").complete();
                        member.getGuild().ban(mentionedMember, 0, TimeUnit.SECONDS).complete();
                    }
                },5,TimeUnit.MINUTES,() -> {});

    }

    private boolean checkModalMember(ModalInteractionEvent modalInteractionEvent, Member member, Message message, String id) {
        if (modalInteractionEvent.getModalId().equals(id)) {
            if (Objects.equals(modalInteractionEvent.getMember(), member)) {
                if (Objects.equals(modalInteractionEvent.getMessage(), message)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkButtonMember(ButtonInteractionEvent buttonInteractionEvent, Member member, CommandClient client, Message message) {
        if (Objects.equals(buttonInteractionEvent.getMember(), member)) {
            if (Objects.equals(buttonInteractionEvent.getMessage(), message)) {
                return true;
            }
        }
        buttonInteractionEvent.reply(Emoji.ERROR + "Вы не можете использовать эту кнопку.").complete();
        return false;
    }

    private boolean checkMenuMember(StringSelectInteractionEvent selectMenuInteractionEvent, Member member, Message message, CommandClient client) {
        if (selectMenuInteractionEvent.getSelectMenu().getId().equals("moderationMenu")) {
            if (Objects.equals(selectMenuInteractionEvent.getMember(), member)) {
                if (selectMenuInteractionEvent.getMessage()
                        .equals(message)) {
                    if (selectMenuInteractionEvent.getChannel()
                            .equals(message.getChannel())) {
                        return true;
                    }
                }
            }
        }
        selectMenuInteractionEvent.reply(Emoji.ERROR + "Это меню не может быть использовано вами.").complete();
        return false;
    }

    private boolean checkBan(User user, Guild guild) {
        try {
            Guild.Ban ban = guild.retrieveBan(user).complete();
            return ban != null;
        }
        catch (ErrorResponseException exception) {
            return false;
        }
    }

    private long getWarnCount(User user) {
        final MongoCollection<Document> collection = database.getCollection("Warns");

        Document getDocument = collection.find(Filters.eq("id", user.getId())).first();

        if (getDocument == null) {
            return 0;
        }

        return getDocument.getInteger("warns");
    }


    private int addWarn(User user) {
        final MongoCollection<Document> collection = database.getCollection("Warns");

        Document getDocument = collection.find(Filters.eq("id", user.getId())).first();

        if (getDocument == null) {
            getDocument = new Document("id",user.getId());
            getDocument.put("warns",1);
            collection.insertOne(getDocument);
            return 1;
        }

        int warns = getDocument.getInteger("warns");
        getDocument.replace("warns", warns+1);
        collection.replaceOne(Filters.eq("id", user.getId()), getDocument);
        return warns+1;
    }

    private int removeWarn(User user) {
        final MongoCollection<Document> collection = database.getCollection("Warns");

        Document getDocument = collection.find(Filters.eq("id", user.getId())).first();

        if (getDocument == null) {
            getDocument = new Document("id",user.getId());
            getDocument.put("warns", 0);
            collection.insertOne(getDocument);
            return 0;
        }

        int warns = getDocument.getInteger("warns");
        if (warns < 1) return 0;
        getDocument.replace("warns", warns-1);
        collection.replaceOne(Filters.eq("id", user.getId()), getDocument);
        return warns-1;
    }

    private void addTimerToDatabase(String type, long time, User user) {
        final MongoCollection<Document> collection = database.getCollection("Timer");

        Document getDocument = collection.find(Filters.eq("id", user.getId()+type)).first();
        if (getDocument == null) {
            getDocument = new Document("id", user.getId()+type);
            getDocument.put("minutes", time);
            collection.insertOne(getDocument);
            return;
        }

        getDocument.replace("minutes", time);
        collection.replaceOne(Filters.eq("id", user.getId()+type), getDocument);
    }

    private void removeTimerFromDatabase(String type, User user) {
        final MongoCollection<Document> collection = database.getCollection("Timer");

        collection.deleteOne(Filters.eq("id", user.getId()+type));
    }

    private long getTimer(String type, User user) {
        final MongoCollection<Document> collection = database.getCollection("Timer");

        Document getDocument = collection.find(Filters.eq("id", user.getId()+type)).first();

        if (getDocument == null) {
            return -1L;
        }

        return getDocument.getLong("minutes");
    }
}
