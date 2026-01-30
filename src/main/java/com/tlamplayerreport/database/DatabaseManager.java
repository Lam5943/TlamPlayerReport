package com.tlamplayerreport.database;

import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;
import com.tlamplayerreport.data.ReportStatus;
import com.tlamplayerreport.data.ReportType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    
    private final ReportPlugin plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(ReportPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean initialize() {
        String databaseType = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        
        try {
            HikariConfig config = new HikariConfig();
            
            if (databaseType.equals("MYSQL")) {
                String host = plugin.getConfig().getString("database.mysql.host");
                int port = plugin.getConfig().getInt("database.mysql.port");
                String database = plugin.getConfig().getString("database.mysql.database");
                String username = plugin.getConfig().getString("database.mysql.username");
                String password = plugin.getConfig().getString("database.mysql.password");
                int poolSize = plugin.getConfig().getInt("database.mysql.pool-size", 10);
                int connectionTimeout = plugin.getConfig().getInt("database.mysql.connection-timeout", 30000);
                int maxLifetime = plugin.getConfig().getInt("database.mysql.max-lifetime", 1800000);
                
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(poolSize);
                config.setConnectionTimeout(connectionTimeout);
                config.setMaxLifetime(maxLifetime);
            } else {
                String fileName = plugin.getConfig().getString("database.sqlite.file-name", "reports.db");
                config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + fileName);
                config.setMaximumPoolSize(1);
            }
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            
            createTables();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void createTables() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS reports (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "reporter_uuid VARCHAR(36) NOT NULL," +
                "reporter_name VARCHAR(16) NOT NULL," +
                "type VARCHAR(16) NOT NULL," +
                "category VARCHAR(32) NOT NULL," +
                "target_uuid VARCHAR(36)," +
                "target_name VARCHAR(16)," +
                "description TEXT," +
                "status VARCHAR(16) NOT NULL," +
                "timestamp BIGINT NOT NULL," +
                "reviewer_name VARCHAR(16)," +
                "notes TEXT" +
                ")";
        
        if (plugin.getConfig().getString("database.type", "SQLITE").equalsIgnoreCase("SQLITE")) {
            createTableSQL = createTableSQL.replace("AUTO_INCREMENT", "AUTOINCREMENT");
        }
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean createReport(Report report) {
        String sql = "INSERT INTO reports (reporter_uuid, reporter_name, type, category, target_uuid, target_name, description, status, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, report.getReporterUuid().toString());
            pstmt.setString(2, report.getReporterName());
            pstmt.setString(3, report.getType().name());
            pstmt.setString(4, report.getCategory());
            pstmt.setString(5, report.getTargetUuid() != null ? report.getTargetUuid().toString() : null);
            pstmt.setString(6, report.getTargetName());
            pstmt.setString(7, report.getDescription());
            pstmt.setString(8, report.getStatus().name());
            pstmt.setLong(9, report.getTimestamp());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setId(generatedKeys.getInt(1));
                }
            }
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY timestamp DESC";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get all reports: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reports;
    }
    
    public List<Report> getReportsByStatus(ReportStatus status) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE status = ? ORDER BY timestamp DESC";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get reports by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reports;
    }
    
    public List<Report> getReportsByPlayer(UUID playerUuid) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE reporter_uuid = ? OR target_uuid = ? ORDER BY timestamp DESC";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, playerUuid.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get reports by player: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reports;
    }
    
    public List<Report> getPendingReportsByPlayer(UUID playerUuid) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE reporter_uuid = ? AND status = ? ORDER BY timestamp DESC";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, ReportStatus.PENDING.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get pending reports by player: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reports;
    }
    
    public void deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reportId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void clearReports() {
        String sql = "DELETE FROM reports";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to clear reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void cleanupOldReports(long cutoffTime) {
        List<String> statuses = plugin.getConfig().getStringList("report-expiry.statuses");
        
        if (statuses.isEmpty()) {
            statuses.add("RESOLVED");
            statuses.add("DISMISSED");
        }
        
        StringBuilder sql = new StringBuilder("DELETE FROM reports WHERE timestamp < ? AND (");
        
        for (int i = 0; i < statuses.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("status = ?");
        }
        sql.append(")");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setLong(1, cutoffTime);
            
            for (int i = 0; i < statuses.size(); i++) {
                pstmt.setString(i + 2, statuses.get(i).toUpperCase());
            }
            
            int deleted = pstmt.executeUpdate();
            
            if (deleted > 0) {
                plugin.getLogger().info("Cleaned up " + deleted + " old reports");
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to cleanup old reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        return Report.builder()
                .id(rs.getInt("id"))
                .reporterUuid(UUID.fromString(rs.getString("reporter_uuid")))
                .reporterName(rs.getString("reporter_name"))
                .type(ReportType.valueOf(rs.getString("type")))
                .category(rs.getString("category"))
                .targetUuid(rs.getString("target_uuid") != null ? UUID.fromString(rs.getString("target_uuid")) : null)
                .targetName(rs.getString("target_name"))
                .description(rs.getString("description"))
                .status(ReportStatus.valueOf(rs.getString("status")))
                .timestamp(rs.getLong("timestamp"))
                .reviewerName(rs.getString("reviewer_name"))
                .notes(rs.getString("notes"))
                .build();
    }
    
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}