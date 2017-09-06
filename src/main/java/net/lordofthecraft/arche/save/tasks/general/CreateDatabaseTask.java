package net.lordofthecraft.arche.save.tasks.general;

import net.lordofthecraft.arche.ArcheTables;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

public class CreateDatabaseTask extends ArcheTask {

    public CreateDatabaseTask() {
    }

    @Override
    public void run() {
        ArcheTables.setUpSQLTables(handle);
    }
}
