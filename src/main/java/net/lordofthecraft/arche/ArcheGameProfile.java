package net.lordofthecraft.arche;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

public class ArcheGameProfile extends WrappedGameProfile
{
    @SuppressWarnings("deprecation")
	private ArcheGameProfile(final String arg0, final String arg1) {
        super(arg0, arg1);
    }
    
    public static WrappedGameProfile rewrap(final WrappedGameProfile profile, final String name, final UUID uuid) {
        final BetterGameProfile better = new BetterGameProfile((GameProfile)profile.getHandle(), name, uuid);
        return WrappedGameProfile.fromHandle((Object)better);
    }
    
    public static WrappedGameProfile reskin(final WrappedGameProfile profile, final PropertyMap props) {
        final ReskinnedGameProfile better = new ReskinnedGameProfile((GameProfile)profile.getHandle(), props);
        return WrappedGameProfile.fromHandle((Object)better);
    }
    
    private static class BetterGameProfile extends GameProfile
    {
        private final GameProfile parent;
        
        private BetterGameProfile(final GameProfile parent, final String name, final UUID uuid) {
            super(uuid, name);
            this.parent = parent;
        }
        
        public PropertyMap getProperties() {
            return this.parent.getProperties();
        }
        
        public String toString() {
            return new ToStringBuilder(this)
            		.append("id", this.getId())
            		.append("name", this.getName())
            		.append("properties", this.parent.getProperties())
            		.append("legacy", this.parent.isLegacy())
            		.toString();
        }
    }
    
    
    private static class ReskinnedGameProfile extends GameProfile
    {
        private final GameProfile parent;
        private final PropertyMap newProperties;
        
        private ReskinnedGameProfile(final GameProfile parent, PropertyMap properties) {
            super(parent.getId(), parent.getName());
            this.parent = parent;
            this.newProperties = properties;
        }
        
        public PropertyMap getProperties() {
        	return this.newProperties;
        }
        
        public String toString() {
            return new ToStringBuilder(this)
            		.append("id", this.getId())
            		.append("name", this.getName())
            		.append("properties", this.getProperties())
            		.append("legacy", this.parent.isLegacy())
            		.toString();
        }
    }
}
