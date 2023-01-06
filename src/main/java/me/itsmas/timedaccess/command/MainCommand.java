package me.itsmas.timedaccess.command;

import me.itsmas.timedaccess.TimedAccess;
import me.itsmas.timedaccess.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class MainCommand implements CommandExecutor
{
    private final TimedAccess plugin;

    public MainCommand(TimedAccess plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        handleCommand(sender, args);
        return true;
    }

    private void handleCommand(CommandSender sender, String[] args)
    {
        if (!Permission.check(sender, "timedaccess.command"))
        {
            return;
        }

        if (args.length == 0)
        {
            Message.COMMAND_USAGE.send(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("give"))
        {
            handleGive(sender, args);
        }
        else if (args[0].equalsIgnoreCase("get"))
        {
            handleGet(sender, args);
        }
        else if (args[0].equalsIgnoreCase("remove"))
        {
            handleRemove(sender, args);
        }
        else if (args[0].equalsIgnoreCase("history"))
        {
            handleHistory(sender, args);
        }else if (args[0].equalsIgnoreCase("booster"))
        {
            handleBoost(sender, args);
        }else if (args[0].equalsIgnoreCase("withdraw"))
        {
            handleWithdraw(sender, args);
        }
        else
        {
            Message.COMMAND_USAGE.send(sender);
        }
    }

    private void handleWithdraw(CommandSender sender, String[] args) {
        if (!Permission.check(sender, "timedaccess.command.withdraw") || Permission.check(sender, "timedaccess.bypass"))
        {
            Message.COMMAND_WITHDRAW_USAGE.send(sender);
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only works if your a player!");
            return;
        }

        if (args.length != 2)
        {
            Message.COMMAND_WITHDRAW_USAGE.send(sender);
            return;
        }

        Player player = (Player) sender;

        int withdrawTime = Integer.parseInt(args[1]);
        long timeInMilis = withdrawTime*60000;

        ItemStack playtimeItem = new ItemStack(Material.CLOCK, 1);
        ItemMeta itemMeta = playtimeItem.getItemMeta();

        itemMeta.setDisplayName(Colour.translate("&x&f&b&7&a&0&4&lP&x&f&b&8&d&0&4&lL&x&f&a&9&f&0&3&lA&x&f&a&b&2&0&3&lY&x&f&9&c&5&0&2&lT&x&f&9&d&8&0&2&lI&x&f&8&e&a&0&1&lM&x&f&8&f&d&0&1&lE\n"));
        itemMeta.setLore(Arrays.asList(Colour.translate("&7Time: &x&c&7&e&b&f&b"+withdrawTime+" min")));

        itemMeta.getPersistentDataContainer().set(TimedAccess.withdrawKey, PersistentDataType.LONG, timeInMilis);

        playtimeItem.setItemMeta(itemMeta);
        plugin.getDataManager().removeTime(player, timeInMilis, "&7[&4-&7] &cPlayer Withdrawal", sender);

        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(playtimeItem);

        if (leftOver.keySet().size()>0) {
            for (ItemStack items : leftOver.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), items);
            }
        }

        Message.COMMAND_WITHDRAW_SUCCESS.send(sender, player.getName(), withdrawTime);
    }

    private void handleBoost(CommandSender sender, String[] args) {
        if (!Permission.check(sender, "timedaccess.command.booster"))
        {
            Message.COMMAND_BOOSTER_USAGE.send(sender);
            return;
        }

        if (args.length != 2)
        {
            Message.COMMAND_BOOSTER_USAGE.send(sender);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        double boost = Double.parseDouble(args[1]);
        TimedAccess.boost = boost;
        plugin.getDataManager().saveBoost(boost);
        Message.COMMAND_BOOSTER_SUCCESS.send(sender, player.getName(), boost);
    }

    private void handleGive(CommandSender sender, String[] args)
    {
        if (!Permission.check(sender, "timedaccess.command.give"))
        {
            return;
        }

        if (args.length < 4)
        {
            Message.COMMAND_GIVE_USAGE.send(sender);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        TimeType type = null;
        if (args[3].equalsIgnoreCase("true")) {
            type = TimeType.GAME;
        } else if (args[3].equalsIgnoreCase("false")) {
            type = TimeType.STORE;
        }

        long time = UtilTime.parseTime(args[2]);

        if (time <= 0L)
        {
            Message.INVALID_TIME.send(sender);
            return;
        }

        if (type!=null && type==TimeType.GAME) {
            time = (long) (time*TimedAccess.boost);
        }

        ArrayList<String> reason = new ArrayList<>();
        reason.addAll(Arrays.asList(args));
        reason.remove(0);
        reason.remove(0);
        reason.remove(0);
        if (type==null) {
            reason.remove(0);
        }

        StringBuilder reason_final = new StringBuilder();
        for (String s : reason) {
            reason_final.append(s).append(" ");
        }
        if (reason.size()==0) {
            reason_final.append("Unknown Reason.");
        }

        plugin.getDataManager().extendTime(player, time, reason_final.toString(), sender);

        String formatted = UtilTime.toHms(time);
        Message.COMMAND_GIVE_SUCCESS.send(sender, player.getName(), formatted);

        if (player.isOnline())
        {
            Message.COMMAND_GET_RECEIVED.send(player.getPlayer(), sender.getName(), formatted);
        }
    }

    private void handleGet(CommandSender sender, String[] args)
    {
        if (!Permission.check(sender, "timedaccess.command.get"))
        {
            Message.COMMAND_GET_USAGE.send(sender);
            return;
        }

        if (args.length != 2)
        {
            Message.COMMAND_GET_USAGE.send(sender);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        String time = UtilTime.toHms(plugin.getDataManager().getTimeRemaining(player));
        Message.COMMAND_GET_SUCCESS.send(sender, player.getName(), time);
    }

    private void handleRemove(CommandSender sender, String[] args)
    {
        if (!Permission.check(sender, "timedaccess.command.remove"))
        {
            Message.COMMAND_REMOVE_USAGE.send(sender);
            return;
        }

        if (args.length != 2)
        {
            Message.COMMAND_REMOVE_USAGE.send(sender);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        plugin.getDataManager().remove(player);
        Message.COMMAND_REMOVE_SUCCESS.send(sender, player.getName());
    }

    private void handleHistory(CommandSender sender, String[] args)
    {
        if (!Permission.check(sender, "timedaccess.command.history"))
        {
            Message.COMMAND_HISTORY_USAGE.send(sender);
            return;
        }

        if (args.length != 2)
        {
            Message.COMMAND_HISTORY_USAGE.send(sender);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        for (String s : plugin.getDataManager().getReasons(player)) {
            UtilReason reason = UtilReason.parseReason(s);
            Message.COMMAND_HISTORY_FORMAT.send(sender, new Date((long)reason.getTimestamp()).toString(), reason.getWho(), UtilTime.toHms(reason.getTimeAdded()), reason.getReason());
        }
    }

    private boolean checkPlayer(Player player, CommandSender sender)
    {
        if (player == null)
        {
            Message.PLAYER_OFFLINE.send(sender);
            return false;
        }

        return true;
    }
}
