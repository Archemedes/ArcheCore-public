package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.magic.MagicData;
import net.lordofthecraft.arche.save.archerows.magic.update.MagicUpdateRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 7/3/2017
 *
 * @author 501warhead
 */
public class MagicAttachment {
    /*
    CREATE TABLE IF NOT EXISTS persona_magics (
    magic_id        INT UNSIGNED AUTO_INCREMENT,
    magic_fk        VARCHAR(255) NOT NULL,
    persona_fk      INT UNSIGNED NOT NULL,
    tier            INT,
    last_advanced   TIMESTAMP DEFAULT NOW(),
    teacher         CHAR(36) DEFAULT NULL,
    learned         TIMESTAMP DEFAULT NOW(),
    visible         BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (magic_id),
    FOREIGN KEY (magic_fk) REFERENCES magics (name) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (persona_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */
    public enum Field {
        TIER("tier", JDBCType.INTEGER),
        LAST_ADVANCED("last_advanced", JDBCType.TIMESTAMP),
        TEACHER("teacher", JDBCType.INTEGER),
        LEARNED("learned", JDBCType.TIMESTAMP),
        VISIBLE("visible", JDBCType.BOOLEAN);

        public final String field;
        public final SQLType type;

        Field(String field, SQLType type) {
            this.field = field;
            this.type = type;
        }
    }

    //private static final SaveHandler buffer = SaveHandler.getInstance();
    private static final IConsumer consumer = ArcheCore.getConsumerControls();
    private final Magic magic;
    private final Persona persona;
    //private FutureTask<MagicData> call;
    private int tier;
    private AtomicBoolean visible;
    private AtomicBoolean taught;
    private Integer teacher;
    private long learned;
    private long lastAdvanced;


    public MagicAttachment(Magic magic, Persona persona, MagicData data) {
        this.magic = magic;
        this.persona = persona;
        tier = data.tier;
        visible = new AtomicBoolean(data.visible);
        taught = new AtomicBoolean(data.taught);
        teacher = data.teacher;
        learned = data.learned;
        lastAdvanced = data.lastAdvanced;

    }

    public void setTier(int i) {
        if (i > magic.getMaxTier()) {
            i = magic.getMaxTier();
        }
        if (i < 0) {
            i = 0;
        }
        this.tier = i;
        this.lastAdvanced = System.currentTimeMillis();
        performSQLUpdate(Field.TIER, i);
    }

    public void setVisible(boolean b) {
        visible.set(b);
        performSQLUpdate(Field.VISIBLE, b);
    }

    public void setTaughtBy(Persona p) {
        taught.set(true);
        teacher = p.getPersonaId();
        performSQLUpdate(Field.TEACHER, p.getPersonaId());
    }

    public void clearTeacher() {
        taught.set(false);
        teacher = null;
        performSQLUpdate(Field.TEACHER, -1);
    }

    public void setLastAdvanced(long time) {
        this.lastAdvanced = time;
        performSQLUpdate(Field.LAST_ADVANCED, time);
    }

    public Magic getMagic() {
        return magic;
    }

    public int getPersonaId() {
        return persona.getPersonaId();
    }

    public Persona getPersona() {
        return persona;
    }

    public int getTier() {
        return tier;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public boolean wasTaught() {
        return taught.get();
    }

    public Integer getTeacherID() {
        return teacher;
    }

    public long getWhenLearned() {
        return learned;
    }

    public long getLastAdvancedTime() {
        return lastAdvanced;
    }

    public BaseComponent[] getReadableLine() {
        return new ComponentBuilder(magic.getName())
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/archehelp " + magic.getName()))
                .event(MessageUtil.hoverEvent(HoverEvent.Action.SHOW_TEXT, "Click to view information on this magic."))
                .create();
    }

    protected void performSQLUpdate(Field field, Object data) {
        //buffer.put(new MagicUpdateTask(persona_id, magic.getName(), field, data));
        consumer.queueRow(new MagicUpdateRow(persona, magic.getName(), field, data));
    }
}
