package me.itsmas.timedaccess.listener;

import me.itsmas.timedaccess.TimedAccess;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RedeemPlaytimeListener implements Listener {

    TimedAccess plugin;

    public RedeemPlaytimeListener(TimedAccess plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action==Action.LEFT_CLICK_AIR || action==Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType()!= Material.CLOCK) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(TimedAccess.withdrawKey, PersistentDataType.LONG)) {
            long timeAdd = meta.getPersistentDataContainer().get(TimedAccess.withdrawKey, PersistentDataType.LONG);
            plugin.getDataManager().extendTime(player, timeAdd, "Redeemed withdrawn playtime", null);
            item.setAmount(item.getAmount()-1);
            player.updateInventory();
        }
    }

}
