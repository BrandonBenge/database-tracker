package com.benge.database.runTest;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

public class testShare {

    public static String testGSLB(String GSLB)
    {
	    	System.out.println("testGSLB: GSLB:"+GSLB);
	    	InetAddress address = null;
	    	String ipAddress = null;
	    	String ipAddressCheck = null;
		try {
			address = InetAddress.getByName(GSLB);
			ipAddress = address.getHostAddress().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println(" ERROR: Could not find a known host!");
			System.exit(1);
		}
	    	System.out.println(" SUCCESS: The address GSLB is pointing at is:"+ipAddress);
	    	System.out.println(" INFO: Lets make sure it never changes. Running a loop to test 1000 times.");
	    	for (int i=0; i<1000; i++) {
	    		try {
	    			address = InetAddress.getByName(GSLB);
	    			ipAddressCheck = address.getHostAddress().toString();
	    		} catch (UnknownHostException e) {
	    			// TODO Auto-generated catch block
	    			System.out.println(" ERROR: Could not find a known host!");
	    			System.exit(1);
	    		}
	    		if (ipAddress.equals(ipAddressCheck))
	    		{
	    			if (i % 100 == 0)
	    			{
	    				System.out.println(" Iteration Number:"+i);
	    			}
	    		} else {
	    			System.out.println(" ERROR: The GSLB is giving me two different IP addresses! Original:'"+ipAddress+"' Current:'"+ipAddressCheck+"'.");
	    			System.exit(1);
	    		}
	    		try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
	    	return ipAddress;
    	}

    public static void testPort(JSONArray HostIPs, int NormalPortInt, int RedirectPortInt, Boolean IsActive, Boolean ActiveMetadata)
    {
    		System.out.println("testPort: HostIPs:"+HostIPs.toString()+" NormalPort:"+NormalPortInt+" RedirectPort:"+RedirectPortInt+" IsActive:"+IsActive+" ActiveMetadata:"+ActiveMetadata);
    		Socket s = null;
    		for (int i=0; i<HostIPs.length(); i++) {
    			String HostIP = HostIPs.getString(i);
    		    try
    		    {
    		        s = new Socket(HostIP, RedirectPortInt);
    		        System.out.println(" SUCCESS: Was able to check the TCP port for host ip at "+HostIP+":"+RedirectPortInt+".");
    		        s.close();
    		        s = new Socket(HostIP, NormalPortInt);
    		        System.out.println(" SUCCESS: Was able to check the TCP port for host ip at "+HostIP+":"+NormalPortInt+".");
    		    }
    		    catch (Exception e)
    		    {
        		    try
        		    {
	    		        s = new Socket(HostIP, NormalPortInt);
	    		        System.out.println(" SUCCESS: Was able to check the TCP port for host ip at "+HostIP+":"+NormalPortInt+".");
        		    }
        		    catch (Exception b)
        		    {
        		    		System.out.println(" ERROR: Was not able to check the TCP port for host ip at "+HostIP+":"+NormalPortInt+".");
        		    }
        		    finally
        		    {
        		        if(s != null)
        		            try {s.close();}
        		            catch(Exception b){}
        		    }
    		    }
    		    finally
    		    {
    		        if(s != null)
    		            try {s.close();}
    		            catch(Exception e){}
    		    }

    		}
    		if (!ActiveMetadata && IsActive)
    		{
    			System.out.println(" ERROR: The GSLB is not pointing at the correct VIP according to the json metadata!");
    		} else if (ActiveMetadata && !IsActive) {
    			System.out.println(" ERROR: The GSLB is not pointing at the correct VIP according to the json metadata!");
    		}

    }
    public static void testLbVip(String Hostname, String IP, int NormalPortInt, Boolean IsActive)
    {
	    	InetAddress address = null;
	    	String ipAddress = null;
	    	Socket s = null;
		try {
			address = InetAddress.getByName(Hostname);
			ipAddress = address.getHostAddress().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println(" ERROR: Could not find a known host!");
			System.exit(1);
		}
		if (IP.equals(ipAddress))
		{
			System.out.println(" SUCCESS: VIP DNS hostname matches the IP you passed in!");
		} else {
			System.out.println(" ERROR: VIP DNS is different then what was passed in! Passed in:'"+IP+"' Current:'"+ipAddress+"'.");
			System.exit(1);
		}
		if (IsActive)
		{
		    try
		    {
		        s = new Socket(IP, NormalPortInt);
		        System.out.println(" SUCCESS: Was able to check the TCP port through the VIP at "+IP+":"+NormalPortInt+".");
		    }
		    catch (Exception e)
		    {
				System.out.println(" ERROR: Was not able to check the TCP port through the VIP at "+IP+":"+NormalPortInt+".");
				System.exit(1);
		    }
		    finally
		    {
		        if(s != null)
		            try {s.close();}
		            catch(Exception e){}
		    }
		} else {
			System.out.println(" INFO: This is not the active side so cant test host "+IP+":"+NormalPortInt+".");
		}
    }
    public static void testEndtoEnd(String GSLB, int NormalPortInt, String MySQLUser, String MySQLPass)
    {
	    	System.out.println("testEndtoEnd: GSLB:"+GSLB);
	    	List<String> returnedHosts = new ArrayList<String>();
	    	for (int i=0; i<1000; i++) {
		    	try {
		    		  returnedHosts.add(getConnection(GSLB, NormalPortInt, MySQLUser, MySQLPass));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			if (i % 100 == 0)
    			{
    				System.out.println(" Iteration Number:"+i);
    			}

	    }
	    	System.out.println("SUCCESS: Count all hosts with frequency, so that we know we are being balanced");
	    	Set<String> uniqueSet = new HashSet<String>(returnedHosts);
	    	for (String temp : uniqueSet) {
	    		System.out.println(" "+temp + ": " + Collections.frequency(returnedHosts, temp));
	    	}

    }

	private static String getConnection(String host, int port, String user, String password) throws SQLException {
		Connection dbConnection = null;
		String queryHost = null;
		Statement stmt = null;
		try {
			Class.forName("org.mariadb.jdbc.Driver").newInstance();
			dbConnection = DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/information_schema?connectTimeout=10000",user,password);
		} catch (SQLTimeoutException e) {
			System.out.println(" WARNING: Connection Timed Out but thats ok, we might be running from far away!");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

		stmt = dbConnection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT SUBSTRING_INDEX(USER(), '@', -1) AS ip,  @@hostname as hostname, @@port as port, DATABASE() as current_database;");
		try {
			while ( rs.next() )
			{
				queryHost = rs.getString("hostname");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dbConnection.close();
		return queryHost;
	}


}
