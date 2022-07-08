import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SQLTable {
    static Logger LOGGER = LoggerFactory.getLogger(SQLTable.class);
    //private final String URL_DATABASE = "";

    public Connection getConnectionBD() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //Connection connection = DriverManager.getConnection("jdbc:sqlite:" + URL_DATABASE);//"myFinance.db"
        try {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

            return DriverManager.getConnection(dbUrl, username, password);
        }
        catch (SQLException | URISyntaxException e) {
            LOGGER.error("Не могу подключиться к БД: " + e.getMessage());
            return null;
        }


        //  return connection;
    }

    //------------------------------To see all notes by the chosen category
    public ArrayList<String> showCatFromBase(Connection connection, String category, long date, String user) throws SQLException {
        String dateOld;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.add(Calendar.MONTH, -1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateOld = df.format(cal.getTime());
        Date dateM = new Date(date);
        String dateNow = df.format(dateM);
        String sql;
        int i = 0;
        ArrayList<String> answer = new ArrayList<>();
        ResultSet rs;
        if (user == null) {
            sql = String.format("SELECT %s, comments, dates, person FROM finance  WHERE %s!=0 and dates>'%s' and dates<'%s';", category, category, dateOld, dateNow);
            LOGGER.debug("showCatFromBase - sql= "+sql);
            PreparedStatement prStatement = connection.prepareStatement(sql);
            rs = prStatement.executeQuery();
            while (rs.next()) {
                answer.add(i + 1 + ") " + rs.getDouble(category) + "_"
                        + rs.getString("comments") + "_" + rs.getString("person") + "_" + rs.getString("dates"));
                i++;
            }
        } else {
            sql = String.format("SELECT %s, comments, dates, person FROM finance  WHERE %s!=0 and dates>'%s' and dates<'%s' and person='%s';", category, category, dateOld, dateNow, user);
            LOGGER.debug("showCatFromBase - sql= "+sql);
            PreparedStatement prStatement = connection.prepareStatement(sql);
            rs = prStatement.executeQuery();
            while (rs.next()) {
                answer.add(i + 1 + ") " + rs.getDouble(category) + "_"
                        + rs.getString("comments") + "_" + rs.getString("dates"));
                i++;
            }
        }

        if (answer.size() == 0) {
            answer.add("По заданной категории данных не найдено!");
        }
        rs.close();
        return answer;
    }

    //------------------------------To see all results from the table for all columns
    public String showTableFromBase(Connection connection, long date, String user)  {

        String dateOld;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.add(Calendar.MONTH, -1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateOld = df.format(cal.getTime());
        Date dateM = new Date(date);
        String dateNow = df.format(dateM);
        String sql;

        double[] summ = new double[Category.values().length];
        if (user == null) {
            sql = String.format("SELECT * FROM finance  WHERE dates>'%s' and dates<'%s';", dateOld, dateNow);
        } else {
            sql = String.format("SELECT * FROM finance  WHERE dates>'%s' and dates<'%s' and person='%s';", dateOld, dateNow, user);
        }
        LOGGER.debug("showTableFromBase - sql= "+sql);
        try {
            ResultSet rs;
            PreparedStatement prStatement = connection.prepareStatement(sql);
            rs = prStatement.executeQuery();
            while (rs.next()) {
                for (int j = 0; j < Category.values().length; j++) {//Category.values().length
                    summ[j] += rs.getDouble(j + 3);
                }
            }
            rs.close();
        }catch(SQLException e){
            LOGGER.error("showTableFromBase"+e.toString());
        }

        String answer = "";
        int y = 0;
        for (Category category : Category.values()) {
            answer += String.valueOf(category) + ": " + summ[y] + "  ";
            y++;
        }

        return answer;
    }

    public Double seeRest(Connection connection, long date, String user) throws SQLException {
        Double answer = 0.0d;
        String dateOld;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.add(Calendar.MONTH, -1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateOld = df.format(cal.getTime());
        Date dateM = new Date(date);
        String dateNow = df.format(dateM);
        double[] summ = new double[Category.values().length];
        double income = 0.0d;
        String sql;
        if (user == null) {
            sql = String.format("SELECT * FROM finance  WHERE dates>'%s' and dates<'%s';", dateOld, dateNow);
        } else {
            sql = String.format("SELECT * FROM finance  WHERE dates>'%s' and dates<'%s' and person='%s';", dateOld, dateNow, user);
        }
        LOGGER.debug("seeRest - sql= "+sql);
        ResultSet rs;
        PreparedStatement prStatement = connection.prepareStatement(sql);
        rs = prStatement.executeQuery();
        while (rs.next()) {
            if (rs.getInt(1) == 5) {
                income += rs.getDouble(2);
            } else {
                for (int j = 0; j < Category.values().length; j++) {//Category.values().length
                    summ[j] += rs.getDouble(j + 3);
                }
            }
        }
        rs.close();
        answer = income;
        for (int i = 0; i < Category.values().length; i++) {
            answer -= summ[i];
        }
        return Double.valueOf(Math.round(answer * 100)) / (100);
    }

    public void createTable(Connection connection) {
        try {
            Statement stmt = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS public.finance\n" +
                    "(\n" +
                    "    income integer,\n" +
                    "    summ numeric(10,3),\n" +
                    "    eda numeric(10,3),\n" +
                    "    sweet numeric(10,3),\n" +
                    "    cafe numeric(10,3),\n" +
                    "    fun numeric(10,3),\n" +
                    "    fuel numeric(10,3),\n" +
                    "    taxes numeric(10,3),\n" +
                    "    toys numeric(10,3),\n" +
                    "    cosmetics numeric(10,3),\n" +
                    "    clothes numeric(10,3),\n" +
                    "    health numeric(10,3),\n" +
                    "    other numeric(10,3),\n" +
                    "    comments character varying(50) COLLATE pg_catalog.\"default\" NOT NULL,\n" +
                    "    dates timestamp without time zone NOT NULL,\n" +
                    "    person character varying(20) COLLATE pg_catalog.\"default\",\n" +
                    "    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),\n" +
                    "    CONSTRAINT finance_pkey PRIMARY KEY (id)\n" +
                    ")\n" +
                    "\n" +
                    "TABLESPACE pg_default;";

            stmt.executeUpdate(sql);
            stmt.close();
            LOGGER.debug("Table created successfully!");
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    public boolean insertInTable(Connection connection, String sql) {

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
            //System.exit(0);
        }
    }

    public boolean deleteFromTableLastInsert(Connection connection, String username) {

        try {
            Statement stmt = connection.createStatement();
            String sql = String.format("DELETE  FROM finance WHERE id in (SELECT id FROM finance WHERE person == \"%s\" ORDER BY dates DESC limit 1);", username);
            stmt.executeUpdate(sql);
            stmt.close();
            return  true;

        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }
}
