package net.lordofthecraft.arche.interfaces;

/**
 * Interface meant to describe logging objects for ArcheCore economy transactions
 * Calling Economy methods requires that you supply a Transaction implementation
 */
public interface Transaction {

    /**
     * A short sentence describing the reason for the transaction
     */
    String getCause();

    /**
     * A human-readable name for the responsible plugin.
     * <br>If you are an Obelisk Module, do <b>not</b> return {@code 'Obelisk'}.
     * Return the name of your Obelisk Module.
     *
     */
    String getRegisteringPluginName();

}
