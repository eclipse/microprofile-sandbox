package application.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class JdbcServlet
 */
@WebServlet("/*")
public class JdbcServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Resource(name = "jdbc/exampleDS")
    DataSource ds1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Statement stmt = null;
        Connection con = null;

        try {
            con = ds1.getConnection();

            stmt = con.createStatement();
            // create a table
            stmt.executeUpdate("create table cities (name varchar(50) not null primary key, population int, county varchar(30))");
            // insert a test record
            stmt.executeUpdate("insert into cities values ('myHomeCity', 106769, 'myHomeCounty')");
            // select a record
            ResultSet result = stmt.executeQuery("select county from cities where name='myHomeCity'");
            result.next();
            // display the county information for the city.
            response.getWriter().print("<h1><font color=green>Text retrieved from database is: </font>" +
                                       "<font color=red>" + result.getString(1) + "</font></h1>");
            //System.out.println("The county for myHomeCity is " + result.getString(1));
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

    }

}
