package org.twinkie.phbot.listeners.runnables;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.twinkie.phbot.config.Constants;

import java.util.Objects;

public class ButtonRunnable {
    public static void ruleAcceptButton(ButtonInteractionEvent buttonInteractionEvent, InteractionHook interactionHook) {
        Button button = buttonInteractionEvent.getButton();
        if (Objects.equals(button.getId(), Constants.ruleButtonId)) {
            Member member = buttonInteractionEvent.getMember();
            assert member != null;
            Guild guild = buttonInteractionEvent.getGuild();
            assert guild != null;
            Role role = guild.getRoleById(Constants.ruleAcceptedRoleId);
            if (role==null || !buttonInteractionEvent.getGuild().getSelfMember().canInteract(role) || !buttonInteractionEvent.getGuild().getSelfMember().canInteract(member)) {
                interactionHook.editOriginal("Произошла ошибка при выдаче, свяжитесь с администрацией.").queue();
                return;
            }
            try {
                guild.addRoleToMember(member, role).reason("Пользователь принял правила").queue();
                interactionHook.editOriginal("Вы приняли правила сервера, просим не нарушать их в будущем.\n**Можете начинать общение!**").queue();
            }
            catch (Exception exception) {
                interactionHook.editOriginal("Произошла ошибка при выдаче, свяжитесь с администрацией.").queue();
                return;
            }
        }
    }
}
