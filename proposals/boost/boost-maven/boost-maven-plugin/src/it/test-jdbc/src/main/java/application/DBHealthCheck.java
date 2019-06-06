package application;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import javax.annotation.Resource;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

@Health
@ApplicationScoped
public class DBHealthCheck implements HealthCheck {
    
    @Resource 
    private DataSource datasource;

    @Override
    public HealthCheckResponse call() {

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("membership");
        try {
            Connection connection = datasource.getConnection();
            boolean isValid = connection.isValid(5);

            DatabaseMetaData metaData = connection.getMetaData();

            responseBuilder = responseBuilder
                        .withData("databaseProductName", metaData.getDatabaseProductName())
                        .withData("databaseProductVersion", metaData.getDatabaseProductVersion())
                        .withData("driverName", metaData.getDriverName())
                        .withData("driverVersion", metaData.getDriverVersion())
                        .withData("isValid", isValid);

            return responseBuilder.state(isValid).build();


        } catch(SQLException  e) {
            responseBuilder = responseBuilder
                   .withData("exceptionMessage", e.getMessage());
            return responseBuilder.down().build();
        }
    }
}
