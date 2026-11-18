package org.example.dragon.config;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.sql.DataSource;
import java.sql.Connection;

/** Применяет миграции Liquibase при старте приложения. Если БД недоступна или миграция упала — деплой падает. */
@WebListener
public class DatabaseMigrationListener implements ServletContextListener {

    private static final String CHANGELOG = "db/changelog/changelog.sql";

    @Resource(lookup = "java:/jdbc/DragonDS")
    private DataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Database migration failed", e);
        }
    }
}
