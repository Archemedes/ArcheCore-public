package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

/**
 * @author 501warhead
 *         A task for logging casket rewards
 */
public class CasketTask extends StatementTask {
    private final Persona pers;
    private final double luck;
    private final long time;
    private final ItemStack[] rewards;

    public CasketTask(Persona pers, double luck, ItemStack[] rewards) {
        this.pers = pers;
        this.luck = luck;
        this.time = System.currentTimeMillis();
        this.rewards = rewards;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setLong(1, time);
        stat.setString(2, pers.getPlayerUUID().toString());
        stat.setInt(3, pers.getId());
        stat.setDouble(4, luck);
        stat.setObject(5, rewards);
    }

    @Override
    protected String getQuery() {
        return "INSERT INTO casket_log VALUES (?,?,?,?,?)";
    }
}
