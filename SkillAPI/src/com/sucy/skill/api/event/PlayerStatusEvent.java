package com.sucy.skill.api.event;

import com.sucy.skill.api.Status;
import com.sucy.skill.skills.PlayerSkills;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player is being inflicted with a status
 */
public class PlayerStatusEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private PlayerSkills player;
    private boolean cancelled;
    private Status status;
    private double duration;

    /**
     * Constructor
     *
     * @param playerData player gaining experience
     * @param status     status being applied
     * @param duration   duration of the status in seconds
     */
    public PlayerStatusEvent(PlayerSkills playerData, Status status, int duration) {
        this.player = playerData;
        this.duration = duration;
        this.status = status;
        this.cancelled = false;
    }

    /**
     * @return data of the player gaining experience
     */
    public PlayerSkills getPlayerData() {
        return player;
    }

    /**
     * @return status being applied
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return duration of the applied status in seconds
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the applied status in seconds
     *
     * @param duration new duration
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * @return whether or not the gain in experience is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether or not the gain in experience is cancelled
     *
     * @param cancelled true/false
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return gets the handlers for the event
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return gets the handlers for the event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
