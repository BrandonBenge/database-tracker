/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benge.database.runTest;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author brandonbenge
 */
public class LBTestFramework {
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        JSONObject lbJsonInfo = new JSONObject();
        try {
            lbJsonInfo = readJSON(args[0]);
            // TODO code application logic here
        } catch (Exception ex) {
            Logger.getLogger(LBTestFramework.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.print("Output json file:" + lbJsonInfo.toString());
        String gslb = lbJsonInfo.getString("gslb");
        JSONObject vips =  lbJsonInfo.getJSONObject("vips");
        
    }
    public static JSONObject readJSON(String fileArg) throws Exception {
        File file = new File(fileArg);
        System.out.println("Loading file:" + fileArg);
        String content = FileUtils.readFileToString(file, "utf-8");
        JSONObject jsonObject = new JSONObject(content);
        return jsonObject;
    }
    

}
