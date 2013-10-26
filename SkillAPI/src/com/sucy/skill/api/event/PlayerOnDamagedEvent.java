package com.sucy.skill.api.event;

import java.util.HashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event for when a player takes damage from an enemy
 */
public class PlayerOnDamagedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private HashMap<String, Object> flags = new HashMap();
    private Player player;
    private LivingEntity attacker;
    private int damage;

    /**
     * Constructor - you shouldn't need to use this
     *
     * @param player player taking damage
     * @param entity mob or player damaging the player
     * @param damage amount of damage dealt
     */
    public PlayerOnDamagedEvent(Player player, LivingEntity entity, int damage) {
        this.player = player;
        this.attacker = entity;
        this.damage = damage;
    }

    /**
     * @return player that took damage
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the entity that damaged the player
     */
    public LivingEntity getAttacker() {
        return attacker;
    }

    /**
     * @return the damage dealt
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Sets the damage dealt to the player
     *
     * @param amount new amount of damage
     */
    public void setDamage(int amount) {
        damage = amount;
    }

    /**
     * @param flag checks if a flag has been set
     * @return     true if set
     */
    public boolean hasFlag(String flag) {
        return flags.containsKey(flag.toLowerCase());
    }

    /**
     * Gets the value set for a flag
     *
     * @param flag flag name
     * @return     flag value
     */
    public Object getFlag(String flag) {
        return flags.get(flag.toLowerCase());
    }

    /**
     * Sets a flag for the event
     *
     * @param flag  flag name
     * @param value flag value
     * @return      previous value or null if there wasn't one
     */
    public Object setFlag(String flag, Object value) {
        return flags.put(flag, value);
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
