/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benge.database.runTest;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author brandonbenge
 */
public class TestwoPassword {
    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        JSONObject jsonInput = new JSONObject();
        try {
        	jsonInput = readJSON(args[0]);
            // TODO code application logic here
        } catch (Exception ex) {
            Logger.getLogger(TestwoPassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Output json file:" + jsonInput.toString());
        JSONObject gslbJson =  jsonInput.getJSONObject("gslb_config");
        String gslb = gslbJson.getString("gslb");
        String ipAddress = testShare.testGSLB(gslb);
        int normalPortInt = 0;
        JSONArray vipsJson =  gslbJson.getJSONArray("vips");
        for (int i=0; i<vipsJson.length(); i++) {
            JSONObject item = vipsJson.getJSONObject(i);
            String hostname = item.getString("hostname");
            String ip = item.getString("ip");
            Boolean activeMetadata = item.getBoolean("active");
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
            testShare.testPort(hostIPs, normalPortInt, redirectPortInt, isActive, activeMetadata); //Test host IPS first to guarantee we are setting to hosts correctly
            testShare.testLbVip(hostname, ip, normalPortInt, isActive);
        }
        System.out.println("SUCCESS: The application has done end to end testing on the LB and everthing is 'Functioning'!");

    }
    public static JSONObject readJSON(String fileArg) throws Exception {
        File file = new File(fileArg);
        System.out.println("Loading file:" + fileArg);
        String content = FileUtils.readFileToString(file, "utf-8");
        JSONObject jsonObject = new JSONObject(content);
        return jsonObject;
    }

}
