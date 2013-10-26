package com.sucy.skill.api.skill;

import com.sucy.skill.SkillAPI;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

import java.util.List;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * <p>Metadata to attach to metadatable objects in case extra effects are
 * needed</p>
 * <p>Metadatable objects can be found here in the inheritance diagram:</p>
 * <p>http://jd.bukkit.org/rb/doxygen/d5/d6f/interfaceorg_1_1bukkit_1_1metadata_1_1Metadatable.html</p>
 *
 */
public class SkillMeta {

    private Metadatable caster;
    private Plugin plugin;

    /**
     * Constructor
     *
     * @param caster caster of the skill
     */
    public SkillMeta(Metadatable caster) {
        this.caster = caster;
        this.plugin = SkillAPI.api;
    }

    /**
     * Constructor
     *
     * @param plugin plugin reference
     * @param caster caster of the skill
     */
    public SkillMeta(Plugin plugin, Metadatable caster) {
        this.caster = caster;
        this.plugin = plugin;
    }

    /**
     * Sets an attribute for the metadata
     *
     * @param name attribute name
     * @param value attribute value
     */
    public void setAttributes(String name, Object value) {
        this.caster.setMetadata(name, new FixedMetadataValue(this.plugin, value));
    }

    /**
     * Retrieves an attribute value
     *
     * @param name attribute name
     * @return attribute value or null if not set
     */
    public Object getAttribute(String name) {
        if (hasAttribute(name)) {
            List<MetadataValue> metaVals = this.caster.getMetadata(name);
            for (MetadataValue i : metaVals) {
                if (i.getOwningPlugin() == this.plugin) {
                    return i.value();
                }
            }
        }
        return null;
    }

        /**
         * Checks if the target has skill meta attached to it
         *
         * @param target target to check
         * @return true if skill meta is present, false otherwise
         */
    

    public boolean hasAttribute(String name) {
        if (caster.hasMetadata(name)) {
            return true;
        }
        return false;
    }
}
