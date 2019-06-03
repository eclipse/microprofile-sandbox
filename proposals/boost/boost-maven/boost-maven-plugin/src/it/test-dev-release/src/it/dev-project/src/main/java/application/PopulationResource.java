package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Servlet implementation class JdbcServlet
 */
@Path("/population")
@ApplicationScoped
public class PopulationResource {

    @Resource
    DataSource ds1;

    @GET
    @Produces("text/plain")
    public String getInformation() throws Exception, IOException {

        String returnValue = null;
        Statement stmt = null;
        Connection con = null;

        try {
            con = ds1.getConnection();

            stmt = con.createStatement();
            // create a table
            stmt.executeUpdate(
                    "create table cities (name varchar(50) not null primary key, population int, county varchar(30))");
            // insert a test record
            stmt.executeUpdate("insert into cities values ('New York City', 8550405, 'Unitest States')");
            // select a record
            ResultSet result = stmt.executeQuery("select population from cities where name='New York City'");
            result.next();

            returnValue = "The population of NYC is " + result.getString(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // drop the table to clean up and to be able to rerun the test.
                stmt.executeUpdate("drop table cities");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return returnValue;
    }

}
