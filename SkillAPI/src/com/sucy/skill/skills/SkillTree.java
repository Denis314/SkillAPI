package com.sucy.skill.skills;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.ClassAttribute;
import com.sucy.skill.api.CustomClass;
import com.sucy.skill.config.ClassValues;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;



/**
 * <p>Class data for classes</p>
 */
public final class SkillTree implements InventoryHolder {

    HashMap<Integer, Skill> skillSlots = new HashMap();
    ChatColor braceColor;
    SkillAPI plugin;
    String parent;
    String prefix;
    String name;
    int width;
    int level;
    int maxLevel;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     * @param name   class name
     * @param config config section to load from
     * @throws SkillTreeException
     */
    public SkillTree(SkillAPI plugin, String name, ConfigurationSection config) throws SkillTreeException {
        this.name = name;
        this.plugin = plugin;
        this.prefix = config.getString(ClassValues.PREFIX).replace('&', ChatColor.COLOR_CHAR);
        this.level = config.getInt(ClassValues.LEVEL);
        this.maxLevel = config.getInt(ClassValues.MAX_LEVEL);
        if (ChatColor.getLastColors(prefix).length() > 0)
            braceColor = ChatColor.getByChar(ChatColor.getLastColors(prefix).charAt(1));
        else braceColor = ChatColor.WHITE;
        if (config.contains(ClassValues.PARENT))
            parent = config.getString(ClassValues.PARENT);


        // Update configuration data
        CustomClass c = plugin.getRegisteredClass(name);
        if (config.contains(ClassValues.HEALTH_BASE))
            c.setBase(ClassAttribute.HEALTH, config.getInt(ClassValues.HEALTH_BASE));
        if (config.contains(ClassValues.HEALTH_BONUS))
            c.setScale(ClassAttribute.HEALTH, config.getInt(ClassValues.HEALTH_BONUS));
        if (config.contains(ClassValues.MANA_BASE))
            c.setBase(ClassAttribute.MANA, config.getInt(ClassValues.MANA_BASE));
        if (config.contains(ClassValues.MANA_BONUS))
             c.setScale(ClassAttribute.MANA, config.getInt(ClassValues.MANA_BONUS));

        ArrayList<Skill> skills = new ArrayList();

        // Inheritance
        for (String tree : config.getStringList(ClassValues.INHERIT)) {
            if (!plugin.hasTree(tree))
                throw new SkillTreeException("Invalid tree inheritance - parent:" + name + ", child:" + tree);
            for (Skill skill : plugin.getClass(tree).skillSlots.values()) {
                skills.add(skill);
            }
        }

        // Included skills
        for (String skill : config.getStringList(ClassValues.SKILLS)) {
            if (!plugin.hasSkill(skill))
                throw new SkillTreeException("Invalid skill name: " + skill);
            skills.add(plugin.getSkill(skill));
        }

        // Arrange the skill tree
        arrange(skills);
    }

    /**
     * @return parent tree
     */
    public String getParent() {
        return parent;
    }

    /**
     * @return class name
     */
    public String getName() {
        return name;
    }

    /**
     * @return max level for the class
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * @return prefix brace color
     */
    public ChatColor getBraceColor() {
        return braceColor;
    }

    /**
     * Gets the skill at the given inventory slot
     *
     * @param slot inventory slot
     * @return     skill at the slot or null if no skill
     */
    public Skill getSkill(int slot) {
        if (!skillSlots.containsKey(slot))
            return null;
        else return skillSlots.get(slot);
    }

    /**
     * Generates a new skill tree inventory
     *
     * @param skills skill level data
     * @return       skill tree inventory
     */
    public Inventory getInventory(PlayerSkills player, HashMap<String, Integer> skills) {
        Inventory inv = plugin.getServer().createInventory(this, width * 9, name);

        for (Map.Entry<Integer, Skill> entry : skillSlots.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().getIndicator(player, skills.get(entry.getValue().getName().toLowerCase())));
        }

        return inv;
    }

    /**
     * Arranges the skill tree
     *
     * @param skills list of skills included in the tree
     * @throws SkillTreeException
     */
    private void arrange(ArrayList<Skill> skills) throws SkillTreeException {
        Collections.sort(skills);
        width = 0;
        int i = 0;
        Skill skill;

        // Cycle through all skills that do not have children, put them
        // at the far left, and branch their children to the right
        while (i < skills.size() && (skill = skills.get(i++)).getSkillReq() == null) {
            skillSlots.put(9 * width, skill);
            width += placeChildren(skills, skill, width * 9 + 1, 0);
        }
    }

    /**
     * Updates a skill in the view
     *
     * @param view   inventory view
     * @param player player
     */
    public void update(Inventory view, PlayerSkills player) {
        for (Map.Entry<Integer, Skill> skills : skillSlots.entrySet()) {
            view.setItem(skills.getKey(), skills.getValue().getIndicator(player, player.getSkillLevel(skills.getValue().getName())));
        }
    }

    /**
     * Places the children of a skill to the right of it, branching downward
     *
     * @param skills skills included in the tree
     * @param skill  skill to add the children of
     * @param slot   slot ID for the first child
     * @param depth  current depth of recursion
     * @return       rows needed to fit the skill and all of its children
     * @throws SkillTreeException
     */
    private int placeChildren(ArrayList<Skill> skills, Skill skill, int slot, int depth) throws SkillTreeException {

        // Prevent going outside the bounds of the inventory
        if (depth == 9)
            throw new SkillTreeException("Error generating the skill tree: " + name + " - too large of a tree!");

        // Add in all children
        int width = 0;
        for (Skill s : skills) {
            if (s.getSkillReq() == null) continue;
            if (s.getSkillReq().equalsIgnoreCase(skill.getName())) {
                skillSlots.put(slot + width * 9, s);
                int w = placeChildren(skills, s, slot + width * 9 + 1, depth + 1);
                width += w;
            }
        }

        // Return the rows needed
        return width == 0 ? 1 : width;
    }

    /**
     * Implemented method just to satisfy the interface
     *
     * @return null
     */
    @Override
    public Inventory getInventory() {
        return null;
    }
}
