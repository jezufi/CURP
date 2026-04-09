/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package curp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConexionPool {
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        // Configura tus datos aquí
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/tu_base");
        config.setUsername("postgres");
        config.setPassword("tu_password");

        // --- OPTIMIZACIONES CRÍTICAS ---
        // Esto hace que los 8,000 registros viajen en paquetes súper eficientes
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        
        config.setMaximumPoolSize(10); 
        config.setConnectionTimeout(30000); // 30 segundos
        config.setLeakDetectionThreshold(2000); // Te avisa si una conexión se queda abierta de más
        
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
