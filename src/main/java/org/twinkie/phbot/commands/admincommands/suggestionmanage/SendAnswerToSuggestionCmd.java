package org.twinkie.phbot.commands.admincommands.suggestionmanage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bson.Document;
import org.twinkie.phbot.commands.commandscategory.AdminCategory;
import org.twinkie.phbot.config.Constants;
import org.twinkie.phbot.config.Emoji;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;
import org.twinkie.phbot.library.commandclient.commons.waiter.EventWaiter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SendAnswerToSuggestionCmd extends AdminCategory {
    private final MongoCollection<Document> documentMongoCollection;
    private final EventWaiter eventWaiter;

    public SendAnswerToSuggestionCmd(MongoDatabase mongoDatabase, EventWaiter eventWaiter) {
        this.documentMongoCollection = mongoDatabase.getCollection("Suggestions");
        ;
        this.eventWaiter = eventWaiter;
        this.name = "suggestionanswer";
        this.aliases = new String[]{"ideaanswer"};
        this.help = "отправить ответ не предложение";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.MANAGE_SERVER};
    }

    @Override
    protected void execute(CommandEvent event) {
        MessageCreateData createData = new MessageCreateBuilder()
                .addContent("Выберите предложение для ответа:")
                .addComponents(getActionRow(documentMongoCollection, event))
                .build();
        AtomicReference<String> messageId = new AtomicReference<>();
        AtomicReference<Message> message = new AtomicReference<>();
        event.reply(createData, replyMessage -> {
            messageId.set(replyMessage.getId());
            message.set(replyMessage);
        });
        User author = event.getAuthor();
        eventWaiter.waitForEvent(StringSelectInteractionEvent.class,
                stringSelectInteractionEvent ->
                        Objects.requireNonNull(stringSelectInteractionEvent.getMember()).getId().equalsIgnoreCase(author.getId())
                                && stringSelectInteractionEvent.getMessageId().equalsIgnoreCase(messageId.get()),
                stringSelectInteractionEvent -> {
                    stringSelectInteractionEvent.deferEdit().queue();
                    long suggestionId = Long.parseLong(Objects.requireNonNull(stringSelectInteractionEvent.getSelectedOptions().get(0).getValue()));
                    buttonWaiter(author, message.get(), suggestionId);
                    message.set(message.get().editMessage("Выберите действие:").setComponents(ActionRow.of(Button.success("yesButton", "Принять"), Button.danger("noButton", "Отклонить"))).complete());
                }, 1, TimeUnit.MINUTES, () -> {
                    List<LayoutComponent> componentList = message.get().getComponents();
                    componentList.replaceAll(LayoutComponent::asDisabled);
                    message.get().editMessageComponents(componentList).queue();
                });
    }

    private void buttonWaiter(User author, final Message message, long suggestionId) {
        eventWaiter.waitForEvent(ButtonInteractionEvent.class,
                buttonInteractionEvent -> buttonInteractionEvent.getUser().getId().equalsIgnoreCase(author.getId())
                        && buttonInteractionEvent.getMessageId().equalsIgnoreCase(message.getId()),
                buttonInteractionEvent -> {
                    buttonInteractionEvent.deferEdit().queue();
                    boolean answer = Objects.equals(buttonInteractionEvent.getButton().getId(), "yesButton");
                    buttonWaiterTwo(author, message.editMessage("Выберите действие:").setComponents(ActionRow.of(Button.success("addAnswerButton", "Добавить причину"), Button.danger("ignoreAnswerButton", "Не добавлять причину"))).complete(), answer, suggestionId);
                }, 30, TimeUnit.SECONDS, () -> message.delete().queue());
    }

    private void buttonWaiterTwo(User author, Message message, boolean answer, long suggestionId) {
        eventWaiter.waitForEvent(ButtonInteractionEvent.class,
                buttonInteractionEvent -> buttonInteractionEvent.getUser().getId().equalsIgnoreCase(author.getId())
                        && buttonInteractionEvent.getMessageId().equalsIgnoreCase(message.getId()),
                buttonInteractionEvent -> {
                    if (buttonInteractionEvent.getButton().getId().equalsIgnoreCase("addAnswerButton"))
                        modalWaiter(author, message, buttonInteractionEvent, suggestionId, answer);
                    else {
                        buttonInteractionEvent.deferEdit().queue();
                        sendAnswer(message, answer, suggestionId, "");
                        message.editMessage(Emoji.SUCCESS + " Ответ на предложение #" + suggestionId + " успешно отправлен.").setComponents(ActionRow.of(disableButton(message.getButtons()))).queue();
                    }
                }, 30, TimeUnit.SECONDS, () -> message.delete().queue());
    }

    private void modalWaiter(User author, Message message, ButtonInteractionEvent buttonInteractionEvent, long suggestionId, boolean answer) {
        TextInput subject = TextInput.create("suggestionAnswer", "Ответ на предложение", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Ваш прекрасный ответ")
                .setMinLength(20)
                .setMaxLength(500)
                .build();

        Modal modal = Modal.create("suggestionAnswerModal", "Создание ответа")
                .addActionRow(subject)
                .build();
        buttonInteractionEvent.replyModal(modal).complete();
        message.editMessageComponents(ActionRow.of(disableButton(message.getButtons()))).queue();
        eventWaiter.waitForEvent(ModalInteractionEvent.class,
                modalInteractionEvent -> modalInteractionEvent.getModalId().equalsIgnoreCase(modal.getId())
                        && modalInteractionEvent.getUser().getId().equalsIgnoreCase(author.getId())
                        && modalInteractionEvent.getMessage().getId().equalsIgnoreCase(message.getId()),
                modalInteractionEvent -> {
                    modalInteractionEvent.deferEdit().queue();
                    String args = modalInteractionEvent.getValues().get(0).getAsString();
                    sendAnswer(message, answer, suggestionId, args);
                    message.editMessage(Emoji.SUCCESS + " Ответ на предложение #" + suggestionId + " успешно отправлен.").setComponents(ActionRow.of(disableButton(message.getButtons()))).queue();
                });
    }

    private List<Button> disableButton(List<Button> buttonList) {
        List<Button> disableButton = new ArrayList<>();
        buttonList.forEach(button -> disableButton.add(button.asDisabled()));
        return disableButton;
    }

    private void sendAnswer(Message message, boolean answer, long suggestionId, String args) {
        Document document = documentMongoCollection.find(new Document("number", suggestionId)).first();
        assert document != null;
        String messageId = document.getString("messageId");
        TextChannel textChannel = message.getGuild().getTextChannelById(Constants.suggestionChannelId);
        assert textChannel != null;
        Message retrivedMessage = textChannel.retrieveMessageById(messageId).complete();
        MessageEmbed messageEmbed = new EmbedBuilder(retrivedMessage.getEmbeds().get(0))
                .clearFields()
                .addField(answer?"Принято":"Отказано", args, false)
                .setColor(answer?Color.GREEN:Color.RED)
                .build();
        retrivedMessage.editMessageEmbeds(messageEmbed).queue();
    }

    private List<ActionRow> getActionRow(MongoCollection<Document> documentMongoCollection, CommandEvent commandEvent) {
        long currentTime = System.currentTimeMillis();
        long daysCount = 259200000L;
        List<Document> documentList = new ArrayList<>();
        documentMongoCollection.find().filter(new Document("guildId", commandEvent.getGuild().getId())).sort(new Document("number", -1)).limit(120).into(documentList);
        for (Document document : documentList) {
            if (currentTime - document.getLong("time") > daysCount) {
                documentList.remove(document);
            }
        }
        Map<Integer, ActionRow> integerActionRowHashMap = new HashMap<>();
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create("suggestionChoice");
        selectMenu.setPlaceholder("Выбор предложения.").setRequiredRange(1, 1).setId("suggestionChoice" + 0);
        int step = 0;
        int stepsCount = 25;
        for (int i = 0; i < documentList.size(); i++) {
            Document document = documentList.get(i);
            User suggestionAuthor = commandEvent.getJDA().retrieveUserById(document.getString("memberId")).complete();
            String authorName = "Unknown";
            if (suggestionAuthor != null) authorName = suggestionAuthor.getGlobalName();
            long suggestionNumber = document.getLong("number");
            selectMenu.addOption("Предложение #" + suggestionNumber + ". От участника " + authorName+".", String.valueOf(suggestionNumber));
            if ((i + 1) % stepsCount == 0) {
                step++;
                integerActionRowHashMap.put(step, ActionRow.of(selectMenu.build()));
                selectMenu = StringSelectMenu.create("suggestionChoice" + (step + 1));
                selectMenu.setPlaceholder("Выбор предложения #" + step + ".").setRequiredRange(1, 1).setId("suggestionChoice" + step);
            } else if (!integerActionRowHashMap.containsKey(step))
                integerActionRowHashMap.put(step, ActionRow.of(selectMenu.build()));
            if (integerActionRowHashMap.size() > 5) break;
        }
        List<ActionRow> actionRowList = new ArrayList<>();
        integerActionRowHashMap.forEach((integer, itemComponents) -> actionRowList.add(itemComponents));
        return actionRowList;
    }
}
