package org.twinkie.phbot.commands.commandscategory;

import net.dv8tion.jda.api.Permission;
import org.twinkie.phbot.library.commandclient.command.Command;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;

public class MemberCommands extends Command {

    public MemberCommands() {
        this.category = new Category("Обычные команды", commandEvent -> commandEvent.getMember().hasPermission(Permission.MESSAGE_SEND));
    }
    @Override
    protected void execute(CommandEvent event) {

    }
}
