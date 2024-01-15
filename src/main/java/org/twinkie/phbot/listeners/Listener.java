package org.twinkie.phbot.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.twinkie.phbot.listeners.runnables.ButtonRunnable;

public class Listener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        ButtonRunnable.ruleAcceptButton(event, event.deferReply(true).complete());
        super.onButtonInteraction(event);
    }

}
