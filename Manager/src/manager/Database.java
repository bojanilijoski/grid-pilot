package manager;

/*

 Derby - Class SimpleApp

 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class Database
{

	private static String framework = "embedded";
	private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private static String protocol = "jdbc:derby:";
	private static Connection conn;
	private static PreparedStatement psInsert = null;
	private static PreparedStatement psUpdate = null;
	private static PreparedStatement psSelect = null;
	private static Statement s = null;
	private static ResultSet rs = null;

	public Database()
	{
		start(null);
	}

	public static void main(String[] args)
	{
		Database.start(args);
	}

	static void start(String[] args)
	{
		if (conn != null)
			return;

		/* parse the arguments to determine which framework is desired */
		parseArguments(args);

		/* load the desired JDBC driver */
		loadDriver();

		try
		{
			Properties props = new Properties(); // connection properties
			// providing a user name and password is optional in the embedded
			// and derbyclient frameworks
			props.put("user", "user");
			props.put("password", "password");

			/*
			 * By default, the schema APP will be used when no username is provided. Otherwise, the
			 * schema name is the same as the user name (in this case "user1" or USER1.)
			 * 
			 * Note that user authentication is off by default, meaning that any user can connect to
			 * your database using any password. To enable authentication, see the Derby Developer's
			 * Guide.
			 */

			String dbName = "MyDB"; // the name of the database

			/*
			 * This connection specifies create=true in the connection URL to cause the database to
			 * be created when connecting for the first time. To remove the database, remove the
			 * directory derbyDB (the same as the database name) and its contents.
			 * 
			 * The directory derbyDB will be created under the directory that the system property
			 * derby.system.home points to, or the current directory (user.dir) if derby.system.home
			 * is not set.
			 */
			conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);

			// We want to control transactions manually. Autocommit is on by
			// default in JDBC.
			conn.setAutoCommit(false);

			/*
			 * Creating a statement object that we can use for running various SQL statements
			 * commands against the database.
			 */
			s = conn.createStatement();
		}
		catch (SQLException sqle)
		{
			printSQLException(sqle);
		}

	}

	private static Connection getConnection()
	{
		if (conn == null)
			Database.start(null);

		return conn;
	}

	private static Statement getStatement()
	{
		if (s == null)
			Database.start(null);

		return s;
	}

	public static void stop()
	{
		/*
		 * In embedded mode, an application should shut down the database. If the application fails
		 * to shut down the database, Derby will not perform a checkpoint when the JVM shuts down.
		 * This means that it will take longer to boot (connect to) the database the next time,
		 * because Derby needs to perform a recovery operation.
		 * 
		 * It is also possible to shut down the Derby system/engine, which automatically shuts down
		 * all booted databases.
		 * 
		 * Explicitly shutting down the database or the Derby engine with the connection URL is
		 * preferred. This style of shutdown will always throw an SQLException.
		 * 
		 * Not shutting down when in a client environment, see method Javadoc.
		 */

		if (framework.equals("embedded"))
		{
			try
			{
				// the shutdown=true attribute shuts down Derby
				DriverManager.getConnection("jdbc:derby:;shutdown=true");

				// To shut down a specific database only, but keep the
				// engine running (for example for connecting to other
				// databases), specify a database in the connection URL:
				// DriverManager.getConnection("jdbc:derby:" + dbName +
				// ";shutdown=true");
			}
			catch (SQLException se)
			{
				if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState()))))
				{
					// we got the expected exception
					System.out.println("Derby shut down normally");
					// Note that for single database shutdown, the expected
					// SQL state is "08006", and the error code is 45000.
				}
				else
				{
					// if the error code or SQLState is different, we have
					// an unexpected exception (shutdown failed)
					System.err.println("Derby did not shut down normally");
					printSQLException(se);
				}
			}
			finally
			{
				// release all open resources to avoid unnecessary memory usage

				// ResultSet
				try
				{
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
				}
				catch (SQLException sqle)
				{
					printSQLException(sqle);
				}

				// Connection
				try
				{
					if (conn != null)
					{
						conn.close();
						conn = null;
					}
				}
				catch (SQLException sqle)
				{
					printSQLException(sqle);
				}
			}
		}
	}

	public static void addAplication(int numberOfPilotJobs, String inputSendbox,
			String outputSendbox, String topApplicationId)
	{
		/*
		 * It is recommended to use PreparedStatements when you are repeating execution of an SQL
		 * statement. PreparedStatements also allows you to parameterize variables. By using
		 * PreparedStatements you may increase performance (because the Derby engine does not have
		 * to recompile the SQL statement each time it is executed) and improve security (because of
		 * Java type checking).
		 */
		// parameter 1 is num (int), parameter 2 is addr (varchar)
		try
		{
			psInsert = getConnection()
					.prepareStatement(
							"insert into applications (number_of_pilot_jobs, input_sendbox, output_sendbox, status, time_start, top_application_id) values (?, ?, ?, ?, ?, ?)");

			psInsert.setInt(1, numberOfPilotJobs);
			psInsert.setString(2, inputSendbox);
			psInsert.setString(3, outputSendbox);
			psInsert.setString(4, "REGISTERED");
			psInsert.setString(5, getTime());
			psInsert.setString(6, topApplicationId);
			psInsert.executeUpdate();

			getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void updateApplication(String id, String status)
	{
		try
		{
			if (status.compareTo(Constants.pilot_job_status_done) == 0)
			{
				psUpdate = getConnection().prepareStatement(
						"update applications set status=?, time_finish=? where id=?");
				psUpdate.setString(1, status);
				psUpdate.setString(2, getTime());
				psUpdate.setInt(3, Integer.parseInt(id));
				psUpdate.executeUpdate();
			}
			else
			{
				psUpdate = getConnection().prepareStatement(
						"update applications set status=? where id=?");

				psUpdate.setString(1, status);
				psUpdate.setInt(2, Integer.parseInt(id));
				psUpdate.executeUpdate();
			}

			getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<String[]> getAllApplications()
	{
		ArrayList<String[]> arrayList = new ArrayList<String[]>();

		try
		{
			rs = getStatement().executeQuery("SELECT * FROM applications ORDER BY time_start DESC");

			while (rs.next())
			{
				arrayList.add(new String[]
				{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8) });
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return arrayList;
	}

	/**
	 * 
	 * @return all applications with status 'registered'
	 */
	public static ArrayList<String[]> getReadyApplications()
	{
		ArrayList<String[]> arrayList = new ArrayList<String[]>();

		try
		{
			rs = getStatement()
					.executeQuery(
							"SELECT * FROM applications WHERE status like 'REGISTERED' ORDER BY time_start DESC");

			while (rs.next())
			{
				arrayList.add(new String[]
				{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8) });
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return arrayList;
	}

	public static String getApplicationOutputSandbox(String applicationId)
	{
		try
		{
			psSelect = getConnection().prepareStatement(
					"SELECT output_sendbox FROM applications WHERE id = ?");

			psSelect.setString(1, applicationId);

			ResultSet rs = psSelect.executeQuery();

			if (rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Add pilot into pilots table
	 * 
	 * @return id of added pilot
	 */
	public static String addPilot(String topApplicationId)
	{
		try
		{
			psInsert = getConnection().prepareStatement(
					"insert into pilots (status, time_start, top_application_id) values (?, ?, ?)");

			psInsert.setString(1, Constants.pilot_job_status_created);
			psInsert.setString(2, getTime());
			psInsert.setString(3, topApplicationId);
			psInsert.executeUpdate();

			getConnection().commit();

			rs = getStatement().executeQuery("SELECT IDENTITY_VAL_LOCAL() FROM pilots");

			if (rs.next())
				return rs.getString(1);

			return "";
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public static void updatePilotStatus(String id, String status)
	{
		try
		{
			if (status.compareTo(Constants.pilot_job_status_available) == 0)
			{
				psUpdate = getConnection().prepareStatement(
						"update pilots set status=?, time_last_check_in=? where id=?");

				psUpdate.setString(1, status);
				psUpdate.setString(2, getTime());
				psUpdate.setString(3, id);
				psUpdate.executeUpdate();

				getConnection().commit();

				return;
			}
			if (status.compareTo(Constants.pilot_job_status_killed) == 0)
			{
				psUpdate = getConnection().prepareStatement(
						"update pilots set status=?, time_finish=? where id=?");

				psUpdate.setString(1, status);
				psUpdate.setString(2, getTime());
				psUpdate.setString(3, id);
				psUpdate.executeUpdate();

				getConnection().commit();

				return;
			}

			psUpdate = getConnection().prepareStatement("update pilots set status=? where id=?");

			psUpdate.setString(1, status);
			psUpdate.setString(2, id);
			psUpdate.executeUpdate();

			getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void updatePilotId(String id, String pilotId)
	{
		try
		{
			psUpdate = getConnection().prepareStatement("update pilots set pilot_id=? where id=?");

			psUpdate.setString(1, pilotId);
			psUpdate.setString(2, id);
			psUpdate.executeUpdate();

			getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static String getPilotStatus(String id)
	{
		try
		{
			psSelect = getConnection().prepareStatement("SELECT status FROM pilots WHERE id = ?");

			psSelect.setString(1, id);

			ResultSet rs = psSelect.executeQuery();

			if (rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public static ArrayList<String[]> getAllPilots()
	{
		ArrayList<String[]> arrayList = new ArrayList<String[]>();

		try
		{
			rs = getStatement().executeQuery("SELECT * FROM pilots ORDER BY id DESC");

			while (rs.next())
			{
				arrayList.add(new String[]
				{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8) });
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return arrayList;
	}

	public static ArrayList<String[]> getAllAvailablePilots()
	{
		ArrayList<String[]> arrayList = new ArrayList<String[]>();

		try
		{
			rs = getStatement().executeQuery(
					"SELECT * FROM pilots WHERE status LIKE 'AVAILABLE' ORDER BY id DESC");

			while (rs.next())
			{
				arrayList.add(new String[]
				{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7) });
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return arrayList;
	}

	public static String getApplicationId(String pilotId)
	{
		try
		{
			psSelect = getConnection().prepareStatement(
					"SELECT application_id FROM pilots WHERE id = ?");

			psSelect.setString(1, pilotId);

			ResultSet rs = psSelect.executeQuery();

			if (rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public static String getTopApplicationId(String pilotId)
	{
		try
		{
			psSelect = getConnection().prepareStatement(
					"SELECT top_application_id FROM pilots WHERE id = ?");

			psSelect.setString(1, pilotId);

			ResultSet rs = psSelect.executeQuery();

			if (rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public static void schedulePilotToApplication(String pilotId, String applicationId)
	{
		try
		{
			psUpdate = getConnection().prepareStatement(
					"update pilots set status='RUNNING', application_id = ? where id = ?");

			psUpdate.setString(1, applicationId);
			psUpdate.setString(2, pilotId);
			psUpdate.executeUpdate();

			getConnection().commit();

			psUpdate = getConnection().prepareStatement(
					"update applications set status='RUNNING' where id=?");

			psUpdate.setString(1, applicationId);
			psUpdate.executeUpdate();

			getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static String getNoOfAvailablePilotJobs(String topApplicationId)
	{
		try
		{
			psSelect = getConnection().prepareStatement(
					"SELECT COUNT(id) FROM pilots WHERE top_application_id = ? AND STATUS LIKE "
							+ Constants.pilot_job_status_available);

			psSelect.setString(1, topApplicationId);

			ResultSet rs = psSelect.executeQuery();

			if (rs.next())
			{
				return rs.getString(1);
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Loads the appropriate JDBC driver for this environment/framework. For example, if we are in
	 * an embedded environment, we load Derby's embedded Driver,
	 * <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
	 */
	public static void loadDriver()
	{
		/*
		 * The JDBC driver is loaded by loading its class. If you are using JDBC 4.0 (Java SE 6) or
		 * newer, JDBC drivers may be automatically loaded, making this code optional.
		 * 
		 * In an embedded environment, this will also start up the Derby engine (though not any
		 * databases), since it is not already running. In a client environment, the Derby engine is
		 * being run by the network server framework.
		 * 
		 * In an embedded environment, any static Derby system properties must be set before loading
		 * the driver to take effect.
		 */
		try
		{
			// EmbeddedDriver driverr = new org.apache.derby.jdbc.EmbeddedDriver();
			Class.forName(driver).newInstance();
		}
		catch (ClassNotFoundException cnfe)
		{
			System.err.println("\nUnable to load the JDBC driver " + driver);
			System.err.println("Please check your CLASSPATH.");
			cnfe.printStackTrace(System.err);
		}
		catch (InstantiationException ie)
		{
			System.err.println("\nUnable to instantiate the JDBC driver " + driver);
			ie.printStackTrace(System.err);
		}
		catch (IllegalAccessException iae)
		{
			System.err.println("\nNot allowed to access the JDBC driver " + driver);
			iae.printStackTrace(System.err);
		}
	}

	/**
	 * Prints details of an SQLException chain to <code>System.err</code>. Details included are SQL
	 * State, Error code, Exception message.
	 * 
	 * @param e
	 *            the SQLException from which to print details.
	 */
	public static void printSQLException(SQLException e)
	{
		// Unwraps the entire exception chain to unveil the real cause of the
		// Exception.
		while (e != null)
		{
			System.err.println("\n----- SQLException -----");
			System.err.println("  SQL State:  " + e.getSQLState());
			System.err.println("  Error Code: " + e.getErrorCode());
			System.err.println("  Message:    " + e.getMessage());
			// for stack traces, refer to derby.log or uncomment this:
			// e.printStackTrace(System.err);
			e = e.getNextException();
		}
	}

	/**
	 * Parses the arguments given and sets the values of this class' instance variables accordingly
	 * - that is which framework to use, the name of the JDBC driver class, and which connection
	 * protocol protocol to use. The protocol should be used as part of the JDBC URL when connecting
	 * to Derby.
	 * <p>
	 * If the argument is "embedded" or invalid, this method will not change anything, meaning that
	 * the default values will be used.
	 * </p>
	 * <p>
	 * 
	 * @param args
	 *            JDBC connection framework, either "embedded", "derbyclient". Only the first
	 *            argument will be considered, the rest will be ignored.
	 */
	private static void parseArguments(String[] args)
	{
		if (args == null)
			return;

		if (args.length > 0)
		{
			if (args[0].equalsIgnoreCase("derbyclient"))
			{
				framework = "derbyclient";
				driver = "org.apache.derby.jdbc.ClientDriver";
				protocol = "jdbc:derby://localhost:1527/";
				// protocol = "jdbc:derby://grid-nagios.ii.edu.mk:1527/";
			}
		}
	}

	private static String getTime()
	{
		Calendar cal = Calendar.getInstance();
		Date creationDate = cal.getTime();
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return date_format.format(creationDate);
	}
}
