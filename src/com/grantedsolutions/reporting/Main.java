/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grantedsolutions.reporting;

import com.grantedsolutions.sql.DataManager;
import java.sql.ResultSet;

/**
 *
 * @author User1
 */
public class Main {

    private static DataManager DM;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Test DB connectivity
        //Test_A();
        
        Test_B();
        
        
        
        
        System.out.println("\nDone.\n");
    }

    
    
    
    
//=========================================================================
    private static void Test_A() {
        System.out.println("-- Test_A -- Database connectivity\n");
        DM = new DataManager();

        try {
            DM.initPackage(DataManager.class, "database2.properties");
        } catch (Exception ex) {
            ;
        }

        ResultSet rm = DM.Execute("SELECT * "
                + "FROM review_data "
                + "WHERE accountID = 1 "
                + "AND projectID = 1 "
                + "AND createdBY = 95466432296386563 "
                + "LIMIT 50");

        DM.DisplayRecords(rm);
    }
    
    
    
    private static void Test_B() {
        CCSS_reporter reporter = new CCSS_reporter();
        
        reporter.accessTree();

    }

}
