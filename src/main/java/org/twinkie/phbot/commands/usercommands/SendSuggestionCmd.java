package org.twinkie.phbot.commands.usercommands;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bson.Document;
import org.twinkie.phbot.commands.commandscategory.MemberCommands;
import org.twinkie.phbot.config.Constants;
import org.twinkie.phbot.config.Emoji;
import org.twinkie.phbot.library.commandclient.command.Command;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;
import org.twinkie.phbot.library.commandclient.command.CooldownScope;
import org.twinkie.phbot.library.commandclient.commons.waiter.EventWaiter;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SendSuggestionCmd extends MemberCommands {
    private final MongoDatabase mongoDatabase;
    private final EventWaiter waiter;

    public SendSuggestionCmd(MongoDatabase mongoDatabase, EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "suggestion";
        this.aliases = new String[]{"idea"};
        this.help = "отправить предложение";
        this.cooldown = 60;
        this.cooldownScope = CooldownScope.USER;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    protected void execute(CommandEvent event) {
        User author = event.getAuthor();
        MessageCreateData messageCreateData = new MessageCreateBuilder()
                .addContent("Укажите предложение по кнопке ниже.")
                .addComponents(ActionRow.of(Button.primary("suggestionButton", "Указать")))
                .build();
        Message message = event.getMessage().reply(messageCreateData).complete();
        TextInput subject = TextInput.create("suggestion", "Предложение", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Ваша прекрасная идея")
                .setMinLength(20)
                .setMaxLength(500)
                .build();

        Modal modal = Modal.create("suggestionModal", "Создание предложения")
                .addActionRow(subject)
                .build();
        waiter.waitForEvent(ButtonInteractionEvent.class,
                buttonInteractionEvent -> buttonInteractionEvent.getUser().getId().equalsIgnoreCase(author.getId())
                        && buttonInteractionEvent.getMessageId().equalsIgnoreCase(message.getId())
                        && Objects.requireNonNull(buttonInteractionEvent.getButton().getId()).equalsIgnoreCase("suggestionButton"),
                buttonInteractionEvent -> {
                    buttonInteractionEvent.replyModal(modal).complete();
                    modalWaiter(author,event,modal);
                    message.editMessageComponents(ActionRow.of(Button.primary("suggestionButton", "Указать").asDisabled())).queue();
                }, 1, TimeUnit.MINUTES, () -> {
                    message.editMessageComponents(ActionRow.of(Button.primary("suggestionButton", "Указать").asDisabled())).queue();
                });
    }

    private void modalWaiter(User author, CommandEvent event, Modal modal) {
        waiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> modalInteractionEvent.getModalId().equalsIgnoreCase(modal.getId())
                && modalInteractionEvent.getUser().getId().equals(author.getId()),
                modalInteractionEvent -> {
                    String args = Objects.requireNonNull(modalInteractionEvent.getValue("suggestion")).getAsString();
                    Guild guild;
                    if (event.isFromType(ChannelType.PRIVATE)) {
                        guild = event.getJDA().getGuildById(Constants.guildId);
                        if (guild == null) {
                            event.replyError(author.getAsMention() + " Произошла ошибка, свяжитесь с администрацией.");
                            return;
                        }
                    } else
                        guild = event.getGuild();
                    TextChannel suggestionChannel = guild.getTextChannelById(Constants.suggestionChannelId);
                    if (suggestionChannel == null || !event.getSelfMember().hasPermission(suggestionChannel, Permission.MESSAGE_SEND)) {
                        event.replyError(author.getAsMention() + " Произошла ошибка, свяжитесь с администрацией.");
                        return;
                    }
                    MongoCollection<Document> documentMongoCollection = mongoDatabase.getCollection("Suggestions");
                    long suggestionsCount = documentMongoCollection.countDocuments() + 1;
                    MessageEmbed messageEmbed = new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setTitle("Предложение #" + suggestionsCount)
                            .setAuthor(author.getGlobalName(), author.getAvatarUrl(), author.getAvatarUrl())
                            .setDescription(args)
                            .setFooter(guild.getName(), guild.getIconUrl())
                            .build();
                    MessageCreateData createData = new MessageCreateBuilder()
                            .addEmbeds(messageEmbed)
                            .build();
                    Message message = suggestionChannel.sendMessage(createData).complete();
                    message.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⬆\uFE0F")).queue();
                    message.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("⬇\uFE0F")).queue();
                    Document document = new Document("number", suggestionsCount)
                            .append("text", args)
                            .append("memberId", author.getId())
                            .append("messageId", message.getId())
                            .append("for", 0)
                            .append("against", 0)
                            .append("time", System.currentTimeMillis())
                            .append("guildId", guild.getId());
                    documentMongoCollection.insertOne(document);
                    modalInteractionEvent.reply(Emoji.SUCCESS + " Ваше предложение отправлено.").setEphemeral(true).queue();
                    event.reactSuccess();
                }, 5, TimeUnit.MINUTES, () -> {});

    }
}
