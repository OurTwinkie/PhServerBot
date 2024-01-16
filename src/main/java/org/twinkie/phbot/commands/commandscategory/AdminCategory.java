package org.twinkie.phbot.commands.commandscategory;

import net.dv8tion.jda.api.Permission;
import org.twinkie.phbot.library.commandclient.command.Command;
import org.twinkie.phbot.library.commandclient.command.CommandEvent;

public class AdminCategory extends Command {
    public AdminCategory() {
        this.category = new Category("Admin", commandEvent -> commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR));
    }
    @Override
    protected void execute(CommandEvent event) {

    }
}
