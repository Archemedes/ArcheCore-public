package net.lordofthecraft.arche.interfaces;

import org.bukkit.plugin.Plugin;

public interface Transaction {

    /**
     * A short sentence describing the reason for the transaction
     */
    String getCause();

    /**
     * Returns the type of transaction this is.
     */
    TransactionType getType();

    /**
     * The {@link Plugin} responsible for this transaction.
     * <br>Use {@code 'Obelisk'} if you are an Obelisk Module.
     *
     * @return {@link Plugin}
     */
    Plugin getRegisteringPlugin();

    /**
     * A human-readable name for the responsible plugin.
     * <br>If you are an Obelisk Module, do <b>not</b> return {@code 'Obelisk'}.
     * Return the name of your Obelisk Module.
     *
     */
    String getRegisteringPluginName();


    enum TransactionType {
        WITHDRAW,
        DEPOSIT,
        SET
    }
}
