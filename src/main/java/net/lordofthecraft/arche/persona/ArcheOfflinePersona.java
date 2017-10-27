package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaRenameEvent;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.update.PersonaUpdateRow;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcheOfflinePersona implements OfflinePersona, InventoryHolder {

    static final IConsumer consumer = ArcheCore.getControls().getConsumer();
    boolean deleted = false;
    final AtomicInteger timePlayed;
    final PersonaKey personaKey;
    final Timestamp creation;
    Timestamp lastPlayed;
    boolean current = false;
    Race race;
    String gender;
    PersonaType type;
    PersonaInventory inv;
    volatile String name;
    Timestamp lastRenamed;
    int pastPlayTime; //stat_playtime_past
    String player;

    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, boolean current, Race race, String gender, PersonaType type, String name) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.current = current;
        this.race = race;
        this.gender = gender;
        this.type = type;
        this.name = name;
        timePlayed = new AtomicInteger(0);
    }

    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, Timestamp lastPlayed, boolean current, Race race, String gender, PersonaType type, String name) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.lastPlayed = lastPlayed;
        this.current = current;
        this.race = race;
        this.gender = gender;
        this.type = type;
        this.name = name;
        timePlayed = new AtomicInteger(0);
    }

    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation) {
        this.personaKey = personaKey;
        this.creation = creation;
        timePlayed = new AtomicInteger(0);
    }

    @Override
    public int getPersonaId() {
        return personaKey.getPersonaID();
    }

    @Override
    public int getSlot() {
        return personaKey.getPersonaSlot();
    }

    @Override
    public boolean isCurrent() {
        return current;
    }

    @Override
    public String getPlayerName() {
        return player;
    }

    @Override
    public PersonaKey getPersonaKey() {
        return personaKey;
    }

    @Override
    public UUID getPlayerUUID() {
        return personaKey.getPlayerUUID();
    }

    @Override
    public Inventory getInventory() {
        if (inv == null || inv.getContents() == null) {
            return null;
        }
        Inventory binv = Bukkit.createInventory(this, InventoryType.PLAYER.getDefaultSize(), "Persona Inventory: " + MessageUtil.identifyPersona(this));
        binv.setContents(inv.getContents());
        return binv;
    }

    @Override
    public Inventory getEnderChest() {
        if (inv == null || inv.getEnderContents() == null) {
            return null;
        }
        Inventory einv = Bukkit.createInventory(this, InventoryType.ENDER_CHEST.getDefaultSize(), "Persona Enderchest: " + MessageUtil.identifyPersona(this));
        einv.setContents(inv.getEnderContents());
        return einv;
    }

    @Override
    public PersonaInventory getPInv() {
        return inv;
    }

    @Override
    public Timestamp getCreationTime() {
        return creation;
    }

    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public void setGender(String gender) {
        this.gender = gender;
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.GENDER, gender, false));
        //buffer.put(new UpdateTask(this, PersonaField.GENDER, gender));
    }

    @Override
    public PersonaType getPersonaType() {
        return type;
    }

    @Override
    public void setPersonaType(PersonaType type) {
        this.type = type;

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.TYPE, type, false));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public void setName(String name) {
        PersonaRenameEvent event = new PersonaRenameEvent(this, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        this.name = name;
        lastRenamed = new Timestamp(System.currentTimeMillis());

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.NAME, name, false));
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.STAT_RENAMED, lastRenamed, false));
    }


    @Override
    public boolean isLoaded() {
        return this instanceof Persona;
    }

    @Override
    public Persona loadPersona(ResultSet res) throws SQLException {
        ArchePersona persona = new ArchePersona(this);
        persona.description = res.getString(PersonaField.DESCRIPTION.field());
        persona.prefix = res.getString(PersonaField.PREFIX.field());
        persona.current = res.getBoolean(PersonaField.CURRENT.field());
        persona.fatigue = res.getInt(PersonaField.FATIGUE.field());
        persona.health = res.getDouble(PersonaField.HEALTH.field());
        persona.food = res.getInt(PersonaField.FOOD.field());
        persona.saturation = res.getFloat(PersonaField.SATURATION.field());

        persona.timePlayed.set(res.getInt(PersonaField.STAT_PLAYED.field()));
        persona.charactersSpoken.set(res.getInt(PersonaField.STAT_CHARS.field()));
        persona.lastRenamed = res.getTimestamp(PersonaField.STAT_RENAMED.field());
        //persona.gainsXP = res.getBoolean(15);
        persona.skills.setMainProfession(ArcheSkillFactory.getSkill(res.getString(PersonaField.SKILL_SELECTED.field())));
        Optional<Creature> creature = ArcheCore.getMagicControls().getCreatureById(res.getString("creature"));
        creature.ifPresent(persona.magics::setCreature);

        String wstr = res.getString(PersonaField.WORLD.field());
        if (!res.wasNull()) {
            UUID wuuid = UUID.fromString(wstr);
            World w = Bukkit.getWorld(wuuid);
            if (w != null) {
                int x = res.getInt(PersonaField.X.field());
                int y = res.getInt(PersonaField.Y.field());
                int z = res.getInt(PersonaField.Z.field());
                persona.location = new WeakBlock(w, x, y, z);
            }
        }
        persona.loadPotionsFromString(res.getString(PersonaField.POTIONS.field()));

        if (ArcheCore.getControls().usesEconomy()) persona.money = res.getDouble(PersonaField.MONEY.field());
        persona.pastPlayTime = res.getInt(PersonaField.STAT_PLAYTIME_PAST.field());

        //We now let all Personas load their skills (albeit lazily). Let's do this now
        persona.loadSkills();

        Connection connection = ArcheCore.getSQLControls().getConnection();

        persona.loadMagics(connection);

        persona.loadTags(connection);

        persona.loadAttributes(connection);

        persona.loadSkin(connection);

        connection.close();
        return persona;
    }

    @Override
    public Persona getPersona() {
        if (!isLoaded()) {
            return null;
        }
        return (Persona) this;
    }

    @Override
    public boolean remove() {
        return false;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(personaKey.getPlayerUUID());
    }

    @Override
    public int getTotalPlaytime() {
        return pastPlayTime + getTimePlayed();
    }

    @Override
    public int getTimePlayed() {
        return timePlayed.get();
    }

    public void setPlayerName(String name) {
        this.player = name;
    }
}
