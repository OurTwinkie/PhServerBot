package org.twinkie.phbot.commands.ModerationCommands;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.EmbedBuilder;
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
import org.bson.Document;
import org.twinkie.phbot.commands.commandscategory.ModerationCategory;
import org.twinkie.phbot.library.commandclient.command.CommandClient;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;
import org.twinkie.phbot.library.commandclient.commons.utils.FinderUtil;
import org.twinkie.phbot.library.commandclient.commons.waiter.EventWaiter;
import org.twinkie.phbot.utils.formatutils.FormatUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ModerationCmd extends ModerationCategory {
    private final EventWaiter waiter;
    private static final String[] adminRoles = Constants.moderationAdminRoles;
    private static final String[] staffRoles = Constants.moderationStaffRoles;
    public ModerationCmd(EventWaiter waiter) {
        this.waiter = waiter;
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
                    selectMenuInteractionEvent.getMessage().editMessage(messageEditBuilder.build()).complete();
                    String value = selectMenuInteractionEvent.getSelectedOptions().get(0).getValue();
                    if (value.equals("ban"))
                    {
                        ban(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient(), adminRoles);
                        return;
                    }
                    if (value.equals("unBan"))
                    {
                        unban(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient(), adminRoles);
                        return;
                    }
                    if (value.equals("kick"))
                    {
                        kick(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient(), adminRoles);
                        return;
                    }
                    if (value.equals("voiceMute"))
                    {
                        voiceMute(mentionedMember, event.getMember(), selectMenuInteractionEvent, event.getClient(), voiceMuteRole, staffRoles);
                        return;
                    }
                    if (value.equals("voiceUnMute"))
                    {
                        voiceUnMute(mentionedMember, event.getMember(), selectMenuInteractionEvent, event.getClient(), voiceMuteRole, staffRoles);
                        return;
                    }
                    if (value.equals("mute"))
                    {
                        textMute(mentionedMember, event.getMember(), selectMenuInteractionEvent, event.getClient(), muteRole, staffRoles);
                        return;
                    }
                    if (value.equals("unMute"))
                    {
                        textUnMute(mentionedMember, event.getMember(), selectMenuInteractionEvent, event.getClient(), muteRole, staffRoles);
                        return;
                    }
                    if (value.equals("warn"))
                    {
                        warn(mentionedMember, event.getMember(), selectMenuInteractionEvent, event.getClient(), localBanRole, staffRoles);
                        return;
                    }
                    if (value.equals("unWarn"))
                    {
                        unWarn(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient(), staffRoles);
                        return;
                    }
                    if (value.equals("info")) {
                        getInfo(mentionedMember.getUser(), event.getMember(), selectMenuInteractionEvent, event.getClient(), staffRoles);
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

    private void getInfo(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на просмотр информации о пользователе.");
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
        time = getTimer("voiceMute", mentionedMember);
        if (time != -1) {
            stringBuilder.append("Время голосового мьюта: `")
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

    private void unLocalBan(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String localBanRoleId, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на снятие локал-бана у пользователя.");
            return;
        }
        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        Role role = member.getGuild().getRoleById(localBanRoleId);
        if (role == null) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Произошла ошибка при выполнении команды. Не найдена роль.");
            return;
        }
        if (!mentioned.getRoles().contains(role)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У пользователя нет локал-бана.");
            return;
        }

        TextInput reason = TextInput.create("windowUnLocalBan", "Причина снятия локал-бана:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину снятия локал-бана здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();


        Modal modal = Modal.create("unLocalBanModal", "Дополнительный ввод")
                .addActionRow(reason)
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowUnLocalBan"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "У пользователя " + FormatUtil.formatUser(mentionedMember) + " был снят локал-бан по причине: " + answer).complete();
                        member.getGuild().removeRoleFromMember(mentionedMember, role).reason(answer  + " UnLocalBan by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "У пользователя " + FormatUtil.formatUser(mentionedMember) + " был снят локал-бан.").complete();
                        member.getGuild().removeRoleFromMember(mentionedMember, role).reason("UnLocalBan by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }

                },5,TimeUnit.MINUTES,() -> {});

    }

    private void localBan(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String localBanRoleId, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на выдачу локал-банов пользователям.");
            return;
        }

        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        Role role = member.getGuild().getRoleById(localBanRoleId);
        if (role == null) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Произошла ошибка при выполнении команды. Не найдена роль.");
            return;
        }
        if (mentioned.getRoles().contains(role)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Пользователь уже получил локал-бан.");
            return;
        }

        TextInput reason = TextInput.create("windowLocalBan", "Причина локал-бан:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину локал-бана здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("localBanModal", "Дополнительный ввод")
                .addActionRow(reason)
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowLocalBan"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователю " + FormatUtil.formatUser(mentionedMember) + " был выдан локал-бан по причине: " + answer).complete();
                        member.getGuild().addRoleToMember(mentionedMember, role).reason(answer  + " Local-Ban by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователю " + FormatUtil.formatUser(mentionedMember) + " был выдан локал-бан.").complete();
                        member.getGuild().addRoleToMember(mentionedMember, role).reason("Local-Ban by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }

                });

    }

    private void warn(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String localBanRoleId, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на выдачу варнов пользователям.");
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder()
                .addContent(client.getSuccess() + " Пользователю " + FormatUtil.formatUser(mentionedMember) + " был выдан варн, текущее количество варнов у пользователя: `" + addWarn(mentionedMember) + "`.");

        selectMenuInteractionEvent.reply(messageCreateBuilder.build()).complete();

        if (getWarnCount(mentionedMember) >= 3) {
            Role role = member.getGuild().getRoleById(localBanRoleId);
            if (role == null) {
                return;
            }
            member.getGuild().addRoleToMember(mentionedMember, role).reason("3 warns").complete();
            return;
        }
    }

    private void unWarn(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на выдачу варнов пользователям.");
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder()
                .addContent(client.getSuccess() + " У пользователя " + FormatUtil.formatUser(mentionedMember) + " был снят варн, текущее количество варнов у пользователя: `" + removeWarn(mentionedMember) + "`.");

        selectMenuInteractionEvent.reply(messageCreateBuilder.build()).complete();
    }

    private void textUnMute(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String muteRoleId, String[] roleAdmin) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на мьют пользователя.");
            return;
        }
        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        Role role = member.getGuild().getRoleById(muteRoleId);
        if (role == null) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Произошла ошибка при выполнении команды. Не найдена роль.");
            return;
        }
        if (!mentioned.getRoles().contains(role)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У пользователя нет мьюта.");
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
                    removeTimerFromDatabase("mute", mentionedMember);
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был размьючен по причине: " + answer).complete();
                        member.getGuild().removeRoleFromMember(mentionedMember, role).reason(answer  + " UnMute by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был размьючен.").complete();
                        member.getGuild().removeRoleFromMember(mentionedMember, role).reason("UnMute by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }

                },5,TimeUnit.MINUTES,() -> {});

    }

    private void textMute(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String muteRoleId, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на мьют пользователя.");
            return;
        }
        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        Role role = member.getGuild().getRoleById(muteRoleId);
        if (role == null) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Произошла ошибка при выполнении команды. Не найдена роль.");
            return;
        }
        if (mentioned.getRoles().contains(muteRoleId)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Пользователь уже получил мьют.");
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
                            addTimerToDatabase("mute", atomicInteger.get(), mentionedMember);
                            if (answer.length() != 0) {
                                modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был замьючен по причине: " + answer + " `На время: " + FormatUtil.formatTime(atomicInteger.get()*60000L) + "`").complete();
                                member.getGuild().addRoleToMember(mentionedMember, role).reason(answer  + " Mute by: " + FormatUtil.formatUser(member.getUser()) + " Time: " + FormatUtil.formatTime(atomicInteger.get()*60000L)).complete();
                            }
                            else {
                                modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был замьючен." + " `На время: " + FormatUtil.formatTime(atomicInteger.get()*60000L) + "`").complete();
                                member.getGuild().addRoleToMember(mentionedMember, role).reason("Mute by: " + FormatUtil.formatUser(member.getUser()) + " Time: " + FormatUtil.formatTime(atomicInteger.get()*60000L)).complete();
                            }
                        }
                        else {
                            ReplyUtil.replyError(modalInteractionEvent, client, "Время не может быть определено, проверьте правильность ввода.");
                        }
                    }

                },5,TimeUnit.MINUTES,() -> {});

    }

    private void voiceUnMute(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String muteRoleId, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на мьют пользователя.");
            return;
        }
        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        Role role = member.getGuild().getRoleById(muteRoleId);
        if (role == null) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Произошла ошибка при выполнении команды. Не найдена роль.");
            return;
        }
        if (!mentioned.getRoles().contains(muteRoleId)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У пользователя нет мьюта.");
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
                    removeTimerFromDatabase("voiceMute", mentionedMember);
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был размьючен по причине: " + answer).complete();
                        member.getGuild().removeRoleFromMember(mentionedMember, role).reason(answer  + " VoiceUnMute by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был размьючен.").complete();
                        member.getGuild().removeRoleFromMember(mentionedMember, role).reason("VoiceUnMute by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }

                },5,TimeUnit.MINUTES,() -> {});

    }

    private void voiceMute(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String muteRoleId, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на мьют пользователя.");
            return;
        }
        Member mentioned = member.getGuild().getMemberById(mentionedMember.getId());
        Role role = member.getGuild().getRoleById(muteRoleId);
        if (role == null) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Произошла ошибка при выполнении команды. Не найдена роль.");
            return;
        }
        if (mentioned.getRoles().contains(role)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Пользователь уже получил мьют.");
            return;
        }

        TextInput reason = TextInput.create("windowVoiceMute", "Причина мьюта:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите причину мьюта здесь")
                .setMinLength(1)
                .setMaxLength(400)
                .setRequired(false)
                .build();

        TextInput time = TextInput.create("windowVoiceMuteTime", "Время мьюта:", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Напишите время мьюта здесь (d - дни, h - часы, m - минуты)")
                .setMinLength(1)
                .setMaxLength(30)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("muteVoiceModal", "Дополнительный ввод")
                .addActionRows(ActionRow.of(reason), ActionRow.of(time))
                .build();
        selectMenuInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, selectMenuInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowVoiceMute"))
                            .getAsString();

                    String timeString = Objects.requireNonNull(modalInteractionEvent.getValue("windowVoiceMuteTime"))
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
                            addTimerToDatabase("voiceMute", atomicInteger.get(), mentionedMember);
                            if (answer.length() != 0) {
                                modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был замьючен по причине: " + answer + " `На время: " + FormatUtil.formatTime(atomicInteger.get()*60000L) + "`").complete();
                                member.getGuild().addRoleToMember(mentionedMember, role).reason(answer  + " VoiceMute by: " + FormatUtil.formatUser(member.getUser()) + " Time: " + FormatUtil.formatTime(atomicInteger.get()*60000L)).complete();
                            }
                            else {
                                modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был замьючен." + " `На время: " + FormatUtil.formatTime(atomicInteger.get()*60000L) + "`").complete();
                                member.getGuild().addRoleToMember(mentionedMember, role).reason("VoiceMute by: " + FormatUtil.formatUser(member.getUser()) + " Time: " + FormatUtil.formatTime(atomicInteger.get()*60000L)).complete();
                            }
                        }
                        else {
                            ReplyUtil.replyError(modalInteractionEvent, client, "Время не может быть определено, проверьте правильность ввода.");
                        }
                    }


                },5,TimeUnit.MINUTES,() -> {});

    }

    private void kick(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на кик пользователей.");
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

    private void unban(User mentionedMember, Member member, StringSelectInteractionEvent selectMenuInteractionEvent, CommandClient client, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на разбан пользователей.");
            return;
        }

        if (!checkBan(mentionedMember, member.getGuild())) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Пользователь не находится в бане.");
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

    private void unban(User mentionedMember, Member member, ButtonInteractionEvent buttonInteractionEvent, CommandClient client, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(buttonInteractionEvent, client, "У вас нет прав на разбан пользователей.");
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
        buttonInteractionEvent.replyModal(modal).complete();

        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> checkModalMember(modalInteractionEvent, member, buttonInteractionEvent.getMessage(), modal.getId()),
                modalInteractionEvent ->
                {
                    String answer = Objects.requireNonNull(modalInteractionEvent.getValue("windowUnBan"))
                            .getAsString();
                    if (answer.length() != 0) {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был разбанен по причине: " + answer).complete();
                        member.getGuild().unban(mentionedMember).reason(answer + " Unban by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                    else {
                        modalInteractionEvent.reply(client.getSuccess() + "Пользователь " + FormatUtil.formatUser(mentionedMember) + " был разбанен.").complete();
                        member.getGuild().unban(mentionedMember).reason("Unban by: " + FormatUtil.formatUser(member.getUser())).complete();
                    }
                },5,TimeUnit.MINUTES,() -> {});

    }

    private void ban(User mentionedMember, Member member, SelectMenuInteractionEvent selectMenuInteractionEvent, CommandClient client, String[] adminRoles) {
        if (checkRolePermission(adminRoles, member)) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "У вас нет прав на бан пользователей.");
            return;
        }

        if (checkBan(mentionedMember, member.getGuild())) {
            ReplyUtil.replyError(selectMenuInteractionEvent, client, "Пользователь уже находится в бане.");
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

    private boolean checkRolePermission(String[] adminRoles, Member member) {
        List<Role> roleList = member.getRoles();
        List<String> stringList = Arrays.asList(adminRoles);
        AtomicBoolean b = new AtomicBoolean(false);
        roleList.forEach(role -> {
            if (stringList.contains(role.getId())) b.set(true);
        });
        return !b.get();
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
        ReplyUtil.replyError(buttonInteractionEvent, client, "Вы не можете использовать эту кнопку.");
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
        ReplyUtil.replyError(selectMenuInteractionEvent, client, "Это меню не может быть использовано вами.");
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
