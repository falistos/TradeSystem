package de.codingair.tradesystem.spigot.commands;

import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.editor.Editor;
import de.codingair.tradesystem.spigot.trade.gui.layout.LayoutManager;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.patterns.DefaultPattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Name;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TradeSystemCMD extends CommandBuilder {
    public TradeSystemCMD() {
        super(TradeSystem.getInstance(), "tradesystem", "Trade-System-CMD", new BaseComponent(Permissions.PERMISSION_MODIFY) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permissions"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Only_for_Player"));
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem", new Lang.P("label", label)));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem", new Lang.P("label", label)));
                return true;
            }
        }, true, "ts");

        LayoutManager l = TradeSystem.getInstance().getLayoutManager();

        getBaseComponent().addChild(new CommandComponent("reload") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                try {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Plugin_Reloading"));
                    String s = Lang.getPrefix() + Lang.get("Success_Plugin_Reloaded");
                    TradeSystem.getInstance().reload();
                    sender.sendMessage(s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        });

        getBaseComponent().addChild(new CommandComponent("layout") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout", new Lang.P("label", label)));
                return true;
            }
        });

        getComponent("layout").addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Create", new Lang.P("label", label)));
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout", "create").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender commandSender, String[] strings, List<String> list) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if (!l.isAvailable(argument)) {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Layout_Name_Already_Exists"));
                    return true;
                }

                try {
                    new Editor(argument, (Player) sender).open();
                } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout").addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Edit", new Lang.P("label", label)));
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout", "edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Pattern layout : l.getPatterns(true)) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = l.getPattern(argument, true);

                if (pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return true;
                }

                try {
                    new Editor(pattern, (Player) sender).open();
                } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout").addChild(new CommandComponent("activate") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Activate", new Lang.P("label", label)));
                return true;
            }
        });

        getComponent("layout", "activate").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Pattern layout : l.getPatterns()) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = l.getPattern(argument);

                if (pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return true;
                }

                if (l.getActive().getName().equals(pattern.getName())) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Already_Activated"));
                    return true;
                }

                l.setActive(pattern.getName());
                sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Activated", new Lang.P("name", pattern.getName())));
                return true;
            }
        });

        getComponent("layout").addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Delete", new Lang.P("label", label)));
                return true;
            }
        });

        getComponent("layout", "delete").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Pattern layout : l.getPatterns(true)) {
                    suggestions.add(layout.getName());
                }

                for (Name name : l.getCrashedPatterns().keySet()) {
                    suggestions.add(name.toString());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = l.getPattern(argument, true);

                if (pattern == null) {
                    Map<?, ?> data = l.getCrashedPatterns().remove(new Name(argument));
                    if (data != null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Deleted", new Lang.P("name", Pattern.deserializeName(new JSON(data)))));
                        return true;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return true;
                }

                // set active layout to default if the active layout is the layout that should be deleted
                if (pattern.equals(l.getActive())) l.setActive(DefaultPattern.NAME);
                l.delete(pattern);

                sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Deleted", new Lang.P("name", pattern.getName())));
                return true;
            }
        });
    }
}
