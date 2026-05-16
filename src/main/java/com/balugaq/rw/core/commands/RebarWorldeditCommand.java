package com.balugaq.rw.core.commands;

import com.balugaq.rw.api.IRebarWorldedit;
import com.balugaq.rw.implementation.RebarWorldedit;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RebarWorldeditCommand {

    private final IRebarWorldedit instance = RebarWorldedit.getInstance();

    private final ClearCommand clearCommand = new ClearCommand(instance);
    private final ClearPosCommand clearPosCommand = new ClearPosCommand(instance);
    private final CloneCommand cloneCommand = new CloneCommand(instance);
    private final ConfirmCommand confirmCommand = new ConfirmCommand(instance);
    private final HelpCommand helpCommand = new HelpCommand(instance);
    private final PasteCommand pasteCommand = new PasteCommand(instance);
    private final ReloadCommand reloadCommand = new ReloadCommand(instance);
    private final RuleCommand ruleCommand = new RuleCommand(instance);
    private final SetPos1Command setPosCommand = new SetPos1Command(instance);
    private final SetPos2Command setPos2Command = new SetPos2Command(instance);
    private final VersionCommand versionCommand = new VersionCommand(instance);

    public final LiteralCommandNode<CommandSourceStack> ROOT = Commands.literal("rebarworldedit")
            .then(clearCommand.get())
            .then(clearPosCommand.get())
            .then(cloneCommand.get())
            .then(confirmCommand.get())
            .then(helpCommand.get())
            .then(pasteCommand.get())
            .then(reloadCommand.get())
            .then(ruleCommand.get())
            .then(setPosCommand.get())
            .then(setPos2Command.get())
            .then(versionCommand.get())
            .build();

    public final LiteralCommandNode<CommandSourceStack> ROOT_ALIAS = Commands.literal("rw")
            .redirect(ROOT)
            .build();
}
