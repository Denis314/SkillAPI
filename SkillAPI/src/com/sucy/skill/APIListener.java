package com.sucy.skill;

import com.sucy.skill.api.*;
import com.sucy.skill.api.event.PlayerOnDamagedEvent;
import com.sucy.skill.api.event.PlayerOnHitEvent;
import com.sucy.skill.api.skill.*;
import com.sucy.skill.language.StatusNodes;
import com.sucy.skill.skills.*;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.Map;

/**
 * <p>Main listener for the API</p>
 * <p>You should not instantiate or use this class</p>
 */
public class APIListener implements Listener {

    private final SkillAPI plugin;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     */
    public APIListener(SkillAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Uses bindings for skill shots
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerSkills data = plugin.getPlayer(player.getName());
        Material heldItem = player.getItemInHand().getType();

        // Must be on right click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        // Cannot be cancelled if clicking on a block
        if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
            return;

        // Must have a valid item
        if (heldItem == null || data.getBound(heldItem) == null || !plugin.isSkillRegistered(data.getBound(heldItem)))
            return;

        // Cast the skill
        data.castSkill(data.getBound(heldItem));
    }

    /**
     * Damage modifier for classes
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {

        // Projectile damage
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile)event.getDamager();
            LivingEntity shooter = projectile.getShooter();
            if (shooter != null && shooter instanceof Player) {
                PlayerSkills player = plugin.getPlayer(((Player) shooter).getName());
                if (player != null && player.getClassName() != null) {
                    CustomClass playerClass = plugin.getRegisteredClass(player.getClassName());

                    // When the default damage isn't 0, set the damage relative
                    // to the default damage to account for varied damages
                    if (CustomClass.getDefaultDamage(projectile.getClass()) != 0) {
                        int damage = (int)event.getDamage() * playerClass.getDamage(projectile.getClass(), player.getName()) / CustomClass.getDefaultDamage(projectile.getClass());
                        event.setDamage(damage < 1 ? 1 : damage);
                    }

                    // Otherwise, just set the damage normally
                    else {
                        int damage = playerClass.getDamage(projectile.getClass(), player.getName());
                        int defaultDamage = CustomClass.getDefaultDamage(projectile.getClass());
                        event.setDamage(event.getDamage() + damage - defaultDamage);
                    }
                }
            }
        }

        // Melee damage
        else if (event.getDamager() instanceof Player) {
            PlayerSkills player = plugin.getPlayer(((Player) event.getDamager()).getName());
            if (player != null && player.getClassName() != null) {
                CustomClass playerClass = plugin.getRegisteredClass(player.getClassName());

                // Set the damage normally
                Material mat = ((Player) event.getDamager()).getItemInHand() == null ?
                        Material.AIR
                        : ((Player) event.getDamager()).getItemInHand().getType();
                int damage = playerClass.getDamage(mat, player.getName());
                int defaultDamage = CustomClass.getDefaultDamage(mat);
                event.setDamage(event.getDamage() + damage - defaultDamage);
            }
        }
    }

    /**
     * Adjust the damage taken depending on the health bar mode
     *
     * @param event event details
     */
    @EventHandler
    public void onDamaged(EntityDamageEvent event) {

        // Adjust damage taken and apply statuses for players
        if (event.getEntity() instanceof Player) {
            String name = ((Player) event.getEntity()).getName();
            PlayerSkills player = plugin.getPlayer(name);

            // If stunned or disarmed, cancel it completely
            if (player.hasStatus(Status.STUN) || player.hasStatus(Status.DISARM)) {
                event.setCancelled(true);

                // Send a message
                Player p = (Player)event.getEntity();
                if (player.hasStatus(Status.STUN)) plugin.sendStatusMessage(p, StatusNodes.STUNNED, player.getTimeLeft(Status.STUN));
                else plugin.sendStatusMessage(p, StatusNodes.DISARMED, player.getTimeLeft(Status.DISARM));
            }

            // Damage modifications only occur when the player has a class and old health bars are enabled
            else if (plugin.oldHealthEnabled() && player.getClassName() != null) {
                CustomClass playerClass = plugin.getRegisteredClass(player.getClassName());
                event.setDamage(event.getDamage() * 20.0 / playerClass.getAttribute(ClassAttribute.HEALTH, player.getLevel()));
            }
        }
    }

    /**
     * Calls events for on-hit effects
     *
     * @param event event details
     */
    @EventHandler (priority =  EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit (EntityDamageByEntityEvent event) {

        // Make sure its the correct causes to avoid infinite loops with the protection checks
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {

            // Get the involved entities in terms of living entities considering projectiles
            LivingEntity damaged = event.getEntity() instanceof LivingEntity ? (LivingEntity) event.getEntity()
                    : null;
            LivingEntity damager = event.getDamager() instanceof LivingEntity ? (LivingEntity) event.getDamager()
                    : event.getDamager() instanceof Projectile ? ((Projectile) event.getDamager()).getShooter()
                    : null;

            // Call an event when a player dealt damage
            if (damager != null && damager instanceof Player && damaged != null) {
                PlayerOnHitEvent e = new PlayerOnHitEvent((Player)damager, damaged, (int)event.getDamage());
                plugin.getServer().getPluginManager().callEvent(e);
                event.setDamage(e.getDamage());
            }

            // Call an event when a player received damage
            if (damaged != null && damaged instanceof Player && damager != null) {
                PlayerOnDamagedEvent e = new PlayerOnDamagedEvent((Player)damaged, damager, (int)event.getDamage());
                plugin.getServer().getPluginManager().callEvent(e);
                event.setDamage(e.getDamage());
            }
        }

        // Status effects
        if (event.getEntity() instanceof Player) {
            PlayerSkills player = plugin.getPlayer(((Player) event.getEntity()).getName());

            // Absorb
            if (player.hasStatus(Status.ABSORB)) {
                event.setCancelled(true);
                Player p = (Player)event.getEntity();
                double health = p.getHealth() + event.getDamage();
                if (health > p.getMaxHealth()) health = p.getMaxHealth();
                p.setHealth(health);
            }

            // Invincible
            else if (player.hasStatus(Status.INVINCIBLE)) {

                // Send a message if applicable
                Player damager = null;
                if (event.getDamager() instanceof Player) damager = (Player)event.getDamager();
                else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    damager = (Player)((Projectile) event.getDamager()).getShooter();
                }
                if (damager != null) plugin.sendStatusMessage(damager, StatusNodes.INVINCIBLE, player.getTimeLeft(Status.INVINCIBLE));

                // Cancel any damage
                event.setCancelled(true);
            }
        }
    }

    /**
     * Loads player data on joining the game
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {

        // If the player data doesn't exist, create a new instance
        if (plugin.getPlayer(event.getPlayer().getName()) == null) {
            plugin.addPlayer(new PlayerSkills(plugin, event.getPlayer().getName()));
        }

        // Otherwise, load the data
        else {
            PlayerSkills skills = plugin.getPlayer(event.getPlayer().getName());

            // Update the player health
            skills.updateHealth();

            // Apply passive skills
            for (Map.Entry<String, Integer> entry : skills.getSkills().entrySet()) {
                if (entry.getValue() >= 1) {
                    ClassSkill s = plugin.getRegisteredSkill(entry.getKey());
                    if (s != null && s instanceof PassiveSkill)
                        ((PassiveSkill) s).onInitialize(event.getPlayer(), entry.getValue());
                }
            }
        }
    }

    /**
     * Cancels passive abilities upon quitting the game
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        PlayerSkills skills = plugin.getPlayer(event.getPlayer().getName());
        skills.stopPassiveAbilities();
        skills.applyMaxHealth(20);
    }

    /**
     * Awards experience for killing a player
     *
     * @param event event details
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            PlayerSkills player = plugin.getPlayer(event.getEntity().getKiller().getName());
            player.giveExp(plugin.getExp(getName(event.getEntity())));
        }
    }

    /**
     * Applies stun/root status effects
     *
     * @param event event details
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        PlayerSkills player = plugin.getPlayer(event.getPlayer().getName());
        if (player != null && (player.hasStatus(Status.STUN) ||  player.hasStatus(Status.ROOT))) {
            event.getPlayer().teleport(event.getFrom());

            // Send a message
            if (player.hasStatus(Status.STUN)) {
                plugin.sendStatusMessage(event.getPlayer(), StatusNodes.STUNNED, player.getTimeLeft(Status.STUN));
            }
            else {
                plugin.sendStatusMessage(event.getPlayer(), StatusNodes.ROOTED, player.getTimeLeft(Status.ROOT));
            }
        }

    }

    /**
     * Handles skill tree interaction
     *
     * @param event event details
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {

        // Make sure its a skill tree inventory
        SkillTree tree = plugin.getClass(event.getInventory().getTitle());
        if (tree != null && event.getInventory().getHolder() instanceof SkillTree) {

            // Do nothing when clicking outside the inventory
            if (event.getSlot() == -999) return;

            boolean top = event.getRawSlot() < event.getView().getTopInventory().getSize();

            // Always cancel the event when clicking in the top region
            if (top) {
                event.setCancelled(true);
                PlayerSkills player = plugin.getPlayer(event.getWhoClicked().getName());
                Skill skill = tree.getSkill(event.getSlot());

                // If they clicked on a skill, try upgrading it
                if (skill != null) {
                    if (player.upgradeSkill(skill)) {
                        tree.update(event.getInventory(), player);
                    }
                }

                // Allow players to take back their items they glitched into the inventory
                else if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    event.setCancelled(false);
                }
            }

            // Do not allow shift clicking items into the inventory
            else if (event.isShiftClick()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Translates an entity into a name to use for the config
     *
     * @param entity entity
     * @return       config name
     */
    private String getName(Entity entity) {
        String name = entity.getClass().getSimpleName().toLowerCase().replace("craft", "");
        if (entity instanceof Skeleton) {
            if (((Skeleton)entity).getSkeletonType() == Skeleton.SkeletonType.WITHER) {
                name = "wither" + name;
            }
        }
        return name;
    }
}
