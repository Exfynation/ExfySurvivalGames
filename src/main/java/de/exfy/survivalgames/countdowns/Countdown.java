package de.exfy.survivalgames.countdowns;

import org.bukkit.scheduler.BukkitTask;

public abstract class Countdown {

    protected int seconds;
    protected BukkitTask task;

    public abstract void onEnable();
    public abstract void onDisable();



}
