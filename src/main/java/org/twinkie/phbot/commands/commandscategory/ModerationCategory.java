package org.twinkie.phbot.commands.commandscategory;

import net.dv8tion.jda.api.Permission;
import org.twinkie.phbot.library.commandclient.command.Command;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;

public class ModerationCategory extends Command {
    public ModerationCategory() {
        this.category = new Category("Команды модерации", commandEvent -> commandEvent.getMember().hasPermission(Permission.MODERATE_MEMBERS));
    }
    @Override
    protected void execute(CommandEvent event) {

    }
}
