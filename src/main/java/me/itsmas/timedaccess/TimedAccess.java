package me.itsmas.timedaccess;

import me.itsmas.timedaccess.command.MainCommand;
import me.itsmas.timedaccess.data.DataManager;
import me.itsmas.timedaccess.listener.LoginListener;
import me.itsmas.timedaccess.listener.RedeemPlaytimeListener;
import me.itsmas.timedaccess.placeholder.PlaceholderHook;
import me.itsmas.timedaccess.task.TimeCheckTask;
import me.itsmas.timedaccess.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimedAccess extends JavaPlugin
{
    private DataManager dataManager;
    private int default_time;

    public static double boost = 1.0;

    public static NamespacedKey withdrawKey;

    @Override
    public void onEnable()
    {

        withdrawKey = new NamespacedKey(this, "withdrawn-playtime");

        preInit();

        dataManager = new DataManager(this);
        default_time = getConfig().getInt("default_playtime");

        new LoginListener(this);
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
        }
        new TimeCheckTask(this);

        getServer().getPluginManager().registerEvents(new RedeemPlaytimeListener(this), this);

        getCommand("timedaccess").setExecutor(new MainCommand(this));

        boost = dataManager.getBoost();
    }

    @Override
    public void onDisable()
    {
        getDataManager().save();
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }

    public int getDefaultTime() {
        return default_time;
    }

    private void preInit()
    {
        saveDefaultConfig();
        Message.init(this);
    }
}
