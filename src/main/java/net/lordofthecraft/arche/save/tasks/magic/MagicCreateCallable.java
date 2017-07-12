package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.persona.MagicAttachment;
import net.lordofthecraft.arche.persona.magic.ArcheMagic;
import net.lordofthecraft.arche.persona.magic.MagicData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created on 7/10/2017
 *
 * @author 501warhead
 */
public class MagicCreateCallable implements Callable<MagicAttachment> {

    private final UUID persona_id;
    private final ArcheMagic magic;
    private final int tier;
    private final UUID teacher;
    private final boolean visible;
    private final SQLHandler handler;
    private MagicAttachment result = null;

    public MagicCreateCallable(UUID persona_id, ArcheMagic magic, int tier, UUID teacher, boolean visible, SQLHandler handler) {
        this.persona_id = persona_id;
        this.magic = magic;
        this.tier = tier;
        this.teacher = teacher;
        this.visible = visible;
        this.handler = handler;
    }

    public MagicCreateCallable(UUID persona_id, ArcheMagic magic, int tier, UUID teacher, boolean visible, SQLHandler handler, MagicAttachment result) {
        this.persona_id = persona_id;
        this.magic = magic;
        this.tier = tier;
        this.teacher = teacher;
        this.visible = visible;
        this.handler = handler;
        this.result = result;
    }

    /*
CREATE TABLE IF NOT EXISTS persona_magics (
    magic_id        INT UNSIGNED AUTO_INCREMENT,
    magic_fk        VARCHAR(255) NOT NULL,
    persona_fk      CHAR(36) NOT NULL,
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

    @Override
    public MagicAttachment call() throws Exception {
        if (result != null) {
            return result;
        }
        String sql = "INSERT INTO persona_magic(magic_fk,persona_fk,tier,teacher,visible) VALUES (?,?,?,?,?);";
        PreparedStatement stat = handler.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stat.setString(1, magic.getName());
        stat.setString(2, persona_id.toString());
        stat.setInt(3, tier);
        stat.setString(4, (teacher == null ? null : teacher.toString()));
        stat.setBoolean(5, visible);
        stat.executeUpdate();
        ResultSet rs = stat.getGeneratedKeys();
        rs.next();
        int key = rs.getInt(1);
        //public MagicData(ArcheMagic magic, int id, int tier, boolean visible, boolean taught, UUID teacher, long learned, long lastAdvanced) {
        MagicData data = new MagicData(magic, key, tier, visible, teacher != null, teacher, System.currentTimeMillis(), System.currentTimeMillis());
        result = new MagicAttachment(magic, persona_id, data);
        rs.close();
        stat.close();
        return result;
    }
}
