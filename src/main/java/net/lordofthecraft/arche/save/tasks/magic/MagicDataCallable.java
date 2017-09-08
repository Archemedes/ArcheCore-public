package net.lordofthecraft.arche.save.tasks.magic;

import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.magic.MagicData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created on 7/3/2017
 *
 * @author 501warhead
 */
public class MagicDataCallable implements Callable<MagicData> {

    private final UUID persona_id;
    private final ArcheMagic magic;
    private final SQLHandler handler;

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

    public MagicDataCallable(UUID persona_id, ArcheMagic magic, SQLHandler handler) {
        this.persona_id = persona_id;
        this.magic = magic;
        this.handler = handler;
    }

    @Override
    public MagicData call() throws Exception {
        String sql = "SELECT tier,last_advanced,teacher,learned FROM persona_magics WHERE persona_id_fk=? AND magic_fk=?";
        MagicData data = null;
        synchronized (handler) {
            PreparedStatement stat = handler.getConnection().prepareStatement(sql);
            stat.setString(1, persona_id.toString());
            stat.setString(2, magic.getName());
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                //public MagicData(ArcheMagic magic, int id, int tier, boolean visible, boolean taught, UUID teacher, long learned, long lastAdvanced) {
                String ss = rs.getString("teacher");
                UUID tuuid = null;
                if (ss != null) {
                    tuuid = UUID.fromString(ss);
                }
                data = new MagicData(
                        magic,
                        rs.getInt("tier"),
                        rs.getBoolean("visible"),
                        tuuid != null,
                        tuuid,
                        rs.getTimestamp("learned").toInstant().toEpochMilli(),
                        rs.getTime("lastAdvanced").toInstant().toEpochMilli()
                );
            }
            rs.close();
            stat.close();
            if (handler instanceof WhySQLHandler) {
                stat.getConnection().close();
            }
        }
        return data;
    }
}
