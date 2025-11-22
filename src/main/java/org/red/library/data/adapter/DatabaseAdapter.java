package org.red.library.data.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseAdapter implements IAdapter {

    private final Config config;
    private final Connection connection;

    public DatabaseAdapter(Config config) throws SQLException {
        this.config = config;
        this.connection = DriverManager.getConnection(getUrl(config.host, config.database, config.port), config.user, config.password);
    }

    public Config getConfig() {
        return this.config;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String tableName() {
        return this.config.owner();
    }

    public abstract String getUrl(String host, String database, int port);

    public abstract String getCreateTableSQL(String plugin);


    //데이터 베이스의 필요 정보들을 담아두기위해 따로 분리해둔 클래스
    public record Config(String host, String database, int port, String user, String password, String owner) {}
}
