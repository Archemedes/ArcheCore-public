package net.lordofthecraft.arche.SQL;

import java.io.*;

public class SQLiteUtils
{
    private String directory;
    private String filename;
    private File file;
    private String extension;
    private SQLite db;
    
    public SQLiteUtils(final SQLite db) {
        super();
        this.extension = ".db";
        this.db = db;
    }
    
    public String getDirectory() {
        return this.directory;
    }
    
    public void setDirectory(final String directory) {
        if (directory == null || directory.length() == 0) {
            this.db.printError("Directory cannot be null or empty.");
        }
        else {
            this.directory = directory;
        }
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public void setFilename(final String filename) {
        if (filename == null || filename.length() == 0) {
            this.db.printError("Filename cannot be null or empty.");
        }
        else if (filename.contains("/") || filename.contains("\\") || filename.endsWith(".db")) {
            this.db.printError("The database filename cannot contain: /, \\, or .db.");
        }
        else {
            this.filename = filename;
        }
    }
    
    public String getExtension() {
        return this.extension;
    }
    
    public void setExtension(final String extension) {
        if (extension == null || extension.length() == 0) {
            this.db.printError("Extension cannot be null or empty.");
        }
        if (extension.charAt(0) != '.') {
            this.db.printError("Extension must begin with a period");
        }
    }
    
    public File getFile() {
        return this.file;
    }
    
    public void setFile(final String directory, final String filename) {
        this.setDirectory(directory);
        this.setFilename(filename);
        final File folder = new File(this.getDirectory());
        if (!folder.exists()) {
            folder.mkdir();
        }
        this.file = new File(folder.getAbsolutePath() + File.separator + this.getFilename() + this.getExtension());
    }
    
    public void setFile(final String directory, final String filename, final String extension) {
        this.setExtension(extension);
        this.setFile(directory, filename);
    }
}
