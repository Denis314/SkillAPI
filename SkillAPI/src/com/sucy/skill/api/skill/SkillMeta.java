package com.sucy.skill.api.skill;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

/**
 * <p>Metadata to attach to metadatable objects in case extra effects are needed</p>
 * <p>Metadatable objects can be found here in the inheritance diagram:</p>
 * <p>http://jd.bukkit.org/rb/doxygen/d5/d6f/interfaceorg_1_1bukkit_1_1metadata_1_1Metadatable.html</p>
 *
 */
public class SkillMeta implements MetadataValue {

    private static final String META_NAME = "skillMeta";

    private HashMap<String, Object> attributes = new HashMap();
    private Player caster;
    private Plugin plugin;

    /**
     * Constructor
     *
     * @param caster caster of the skill
     */
    public SkillMeta(Player caster) {
        this.caster = caster;
    }

    /**
     * Constructor
     *
     * @param plugin plugin reference
     * @param caster caster of the skill
     */
    public SkillMeta(Plugin plugin, Player caster) {
        this.caster = caster;
        this.plugin = plugin;
    }

    /**
     * Attaches this meta data to the target
     *
     * @param target target to attach to
     */
    public void attach(Metadatable target) {
        target.setMetadata(META_NAME, this);
    }

    /**
     * Sets an attribute for the metadata
     *
     * @param name  attribute name
     * @param value attribute value
     */
    public void setAttributes(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Retrieves an attribute value
     *
     * @param name attribute name
     * @return     attribute value or null if not set
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * @return caster
     */
    @Override
    public Object value() {
        return caster;
    }

    /**
     * @return caster hash code
     */
    @Override
    public int asInt() {
        return caster.hashCode();
    }

    /**
     * @return caster hash code
     */
    @Override
    public float asFloat() {
        return caster.hashCode();
    }

    /**
     * @return caster hash code
     */
    @Override
    public double asDouble() {
        return caster.hashCode();
    }

    /**
     * @return caster hash code
     */
    @Override
    public long asLong() {
        return caster.hashCode();
    }

    /**
     * @return 0
     */
    @Override
    public short asShort() {
        return 0;
    }

    /**
     * @return 0
     */
    @Override
    public byte asByte() {
        return 0;
    }

    /**
     * @return true if caster isn't null
     */
    @Override
    public boolean asBoolean() {
        return caster != null;
    }

    /**
     * @return caster name
     */
    @Override
    public String asString() {
        return caster.getName();
    }

    /**
     * @return plugin or null if not set
     */
    @Override
    public Plugin getOwningPlugin() {
        return plugin;
    }

    /**
     * Does nothing
     */
    @Override
    public void invalidate() {}

    /**
     * Checks if the target has skill meta attached to it
     *
     * @param target target to check
     * @return       true if skill meta is present, false otherwise
     */
    public static boolean hasMeta(Metadatable target) {
        return target.hasMetadata(META_NAME);
    }

    /**
     * Retrieves the skill meta from the target
     *
     * @param target target to retrieve meta from
     * @return       meta attached or null if none
     */
    public static SkillMeta getMeta(Metadatable target) {
        if (hasMeta(target)) return (SkillMeta)target.getMetadata(META_NAME);
        else return null;
    }
}
