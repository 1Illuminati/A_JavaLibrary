package org.red.library.data.adapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.red.library.data.serialize.SerializeDataMap;

public class MySqlAdapter extends DatabaseAdapter {
    public MySqlAdapter(Config config) throws SQLException {
        super(config);

        try (Statement stmt = super.getConnection().createStatement()) {
            stmt.executeUpdate(getCreateTableSQL(tableName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SerializeDataMap loadDataMap(String key) {
        String loadSQL = "SELECT `data` FROM `" + tableName() + "` WHERE `key` = ?";

        try (PreparedStatement ps = super.getConnection().prepareStatement(loadSQL)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery(); 
            if (rs.next()) return SerializeDataMap.stringToSerialzableDataMap(rs.getString("data"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        
        return null;
    }

    @Override
    public void saveDataMap(String key, SerializeDataMap value) {
        String saveSQL = "INSERT INTO `" + tableName() + "` (`key`, `data`) VALUES (?, ?) " + "ON DUPLICATE KEY UPDATE `data` = VALUES(`data`)";
        try (PreparedStatement ps = super.getConnection().prepareStatement(saveSQL)) {
            ps.setString(1, key);
            ps.setString(2, value.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean containDataMap(String key) {
        String containSQL = "SELECT 1 FROM `" + tableName() + "` WHERE `key` = ? LIMIT 1";
        try (PreparedStatement ps = super.getConnection().prepareStatement(containSQL)) {
            ps.setString(1, key);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void deleteDataMap(String key) {
        String deleteSQL = "DELETE FROM `" + tableName() + "` WHERE `key` = ?";
        try (PreparedStatement ps = super.getConnection().prepareStatement(deleteSQL)) {
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCreateTableSQL(String table) {
        return "CREATE TABLE IF NOT EXISTS `" + table + "` ("
                + "`key` VARCHAR(255) NOT NULL, "
                + "`data` LONGTEXT NOT NULL, "
                + "PRIMARY KEY (`key`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
    }

    @Override
    public String getUrl(String host, String database, int port) {
        return String.format("jdbc:%s://%s:%d/%s?useSSL=false&serverTimezone=UTC", "mysql", host, port, database);
    }

    @Override
    public Set<String> loadAllKey() {
        Set<String> keys = new HashSet<>();
        String sql = "SELECT `key` FROM `" + tableName() + "`;";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                keys.add(resultSet.getString("key"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return keys;
    }

}
