/**
 * 
 */
package org.smartbuy.sar.SarRest.DAO;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;


public class DbConnection {
	public static final Logger logger = Logger.getLogger(DbConnection.class);
	private static DataSource dataSource;
	private static Connection connection = null;

	/**
	 * Returns a database connection	  
	 */
	public static Connection createConnection() {
		Context context = null;
		try {
			context = new InitialContext();
			dataSource = (DataSource) context
					.lookup("java:comp/env/jdbc/new_temp");
			connection = dataSource.getConnection();

		} catch (NamingException e) {
			logger.debug("Exception thrown" , e);
		} catch (SQLException e) {
			logger.debug("Exception thrown" , e);		
		}
		logger.info("Connection returned successfully");
		return connection;
	}
}