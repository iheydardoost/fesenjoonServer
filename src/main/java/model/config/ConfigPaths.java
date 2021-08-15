package model.config;

public class ConfigPaths {
    private String serverSocketConfigPath;
    private String dbConnectionConfigPath;

    public ConfigPaths() {
    }

    public ConfigPaths(String serverSocketConfigPath, String dbConnectionConfigPath) {
        this.serverSocketConfigPath = serverSocketConfigPath;
        this.dbConnectionConfigPath = dbConnectionConfigPath;
    }

    public String getServerSocketConfigPath() {
        return serverSocketConfigPath;
    }

    public void setServerSocketConfigPath(String serverSocketConfigPath) {
        this.serverSocketConfigPath = serverSocketConfigPath;
    }

    public String getDbConnectionConfigPath() {
        return dbConnectionConfigPath;
    }

    public void setDbConnectionConfigPath(String dbConnectionConfigPath) {
        this.dbConnectionConfigPath = dbConnectionConfigPath;
    }
}
