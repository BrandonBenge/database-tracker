/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benge.database.runTest;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author brandonbenge
 */
public class TestFramework {
    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        JSONObject jsonInput = new JSONObject();
        try {
        	jsonInput = readJSON(args[0]);
            // TODO code application logic here
        } catch (Exception ex) {
            Logger.getLogger(TestFramework.class.getName()).log(Level.SEVERE, null, ex);
        }
        String mysqlUser = args[1];
        String mysqlPass = args[2];
        System.out.println("Output json file:" + jsonInput.toString());
        JSONObject gslbJson =  jsonInput.getJSONObject("gslb_config");
        String gslb = gslbJson.getString("gslb");
        String ipAddress = testGSLB(gslb);
        int normalPortInt = 0;
        JSONArray vipsJson =  gslbJson.getJSONArray("vips");
        for (int i=0; i<vipsJson.length(); i++) {
            JSONObject item = vipsJson.getJSONObject(i);
            String hostname = item.getString("hostname");
            String ip = item.getString("ip");
            Boolean isActive = null;
            if (!ipAddress.equals(ip))
            {
            		isActive = false;
            } else {
            		isActive = true;
            }
            String normalPort = item.getString("normalPort");
            normalPortInt = Integer.parseInt(normalPort);
            String redirectPort = item.getString("redirectPort");
            int redirectPortInt = Integer.parseInt(redirectPort);
            JSONArray hostIPs = item.getJSONArray("hostIPs");
            testPort(hostIPs, normalPortInt, redirectPortInt, isActive); //Test host IPS first to guarantee we are setting to hosts correctly
            System.out.println(" SUCCESS: Proved that all the hosts are set correctly");
            testLbVip(hostname, ip, normalPortInt, isActive);
        }
        testEndtoEnd(gslb, normalPortInt, mysqlUser, mysqlPass);
        System.out.println("SUCCESS: The application has done end to end testing on the LB and everthing is 'Functioning'!");

    }
    public static JSONObject readJSON(String fileArg) throws Exception {
        File file = new File(fileArg);
        System.out.println("Loading file:" + fileArg);
        String content = FileUtils.readFileToString(file, "utf-8");
        JSONObject jsonObject = new JSONObject(content);
        return jsonObject;
    }

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

    public static void testPort(JSONArray HostIPs, int NormalPortInt, int RedirectPortInt, Boolean IsActive)
    {
    		System.out.println("testPort: HostIPs:"+HostIPs.toString()+" NormalPort:"+NormalPortInt+" RedirectPort:"+RedirectPortInt+" IsActive:"+IsActive);
    		Socket s = null;
    		for (int i=0; i<HostIPs.length(); i++) {
    			String HostIP = HostIPs.getString(i);
	    		if (IsActive)
	    		{
	    		    try
	    		    {
	    		        s = new Socket(HostIP, RedirectPortInt);
	    		        System.out.println(" SUCCESS: Was able to check the TCP port for host ip at "+HostIP+":"+RedirectPortInt+".");
	    		    }
	    		    catch (Exception e)
	    		    {
	    				System.out.println(" ERROR: Was not able to check the TCP port for host ip at "+HostIP+":"+RedirectPortInt+".");
	    				System.exit(1);
	    		    }
	    		    finally
	    		    {
	    		        if(s != null)
	    		            try {s.close();}
	    		            catch(Exception e){}
	    		    }
	    		} else {
	    		    try
	    		    {
	    		        s = new Socket(HostIP, NormalPortInt);
	    		        System.out.println(" SUCCESS: Was able to check the TCP port for host ip at "+HostIP+":"+NormalPortInt+".");
	    		    }
	    		    catch (Exception e)
	    		    {
	    				System.out.println(" ERROR: Was not able to check the TCP port for host ip at "+HostIP+":"+NormalPortInt+".");
	    				System.exit(1);
	    		    }
	    		    finally
	    		    {
	    		        if(s != null)
	    		            try {s.close();}
	    		            catch(Exception e){}
	    		    }
	    		}
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
			System.out.println(" INFO: This is not the active side so cant test host "+IP+".");
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
			dbConnection = DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/information_schema?connectTimeout=500",user,password);
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
