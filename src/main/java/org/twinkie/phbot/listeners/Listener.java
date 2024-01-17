package org.twinkie.phbot.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.twinkie.phbot.config.Constants;
import org.twinkie.phbot.listeners.runnables.ButtonRunnable;

import java.util.Objects;

public class Listener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();
        if (Objects.equals(button.getId(), Constants.ruleButtonId)) {
            ButtonRunnable.ruleAcceptButton(event, event.deferReply(true).complete());
        }
        super.onButtonInteraction(event);
    }

}
