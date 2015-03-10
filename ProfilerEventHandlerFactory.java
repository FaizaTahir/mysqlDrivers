package mysqldriver;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEventHandler;

/**
* @author mmatthew
*/
public class ProfilerEventHandlerFactory {

	private static final Map CONNECTIONS_TO_SINKS = new HashMap();

	private Connection ownerConnection = null;

	private Log log = null;

	/**
	 * Returns the ProfilerEventHandlerFactory that handles profiler events for the given
	 * connection.
	 * 
	 * @param conn
	 *            the connection to handle events for
	 * @return the ProfilerEventHandlerFactory that handles profiler events
	 */
	public static synchronized ProfilerEventHandler getInstance(ConnectionImpl conn) throws SQLException {
		ProfilerEventHandler handler = (ProfilerEventHandler) CONNECTIONS_TO_SINKS
				.get(conn);

		if (handler == null) {
			handler = (ProfilerEventHandler)Util.getInstance(conn.getProfilerEventHandler(), new Class[0], new Object[0], conn.getExceptionInterceptor());
			
			// we do it this way to not require
			// exposing the connection properties 
			// for all who utilize it
			conn.initializeExtension(handler);
			
			CONNECTIONS_TO_SINKS.put(conn, handler);
		}

		return handler;
	}

	public static synchronized void removeInstance(Connection conn) {
		ProfilerEventHandler handler = (ProfilerEventHandler) CONNECTIONS_TO_SINKS.remove(conn);
		
		if (handler != null) {
			handler.destroy();
		}
	}

	private ProfilerEventHandlerFactory(Connection conn) {
		this.ownerConnection = conn;

		try {
			this.log = this.ownerConnection.getLog();
		} catch (SQLException sqlEx) {
			throw new RuntimeException("Unable to get logger from connection");
		}
	}
}
