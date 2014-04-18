/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grantedsolutions.reporting;

import com.grantedsolutions.sql.DataManager;
import java.sql.ResultSet;

import com.learnerati.datameme.*;
import com.grantedsolutions.chart.*;
import com.grantedsolutions.utilities.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        
        
        // Pull Review_data for English grade 2
        //Test_B();
        
        // Draw a rw CR Chart
        Test_C();
        
        
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
    
    
    private static void Test_C() {
        CCSS_reporter reporter = new CCSS_reporter();
        
        DMemeGrid dataGrid = reporter.getData();
        
        dataGrid.DumpGrid();
        
        // define some rules to use for the chart
        Map<String,Object> rules = new HashMap<>();                
        rules.put("OutFileName", "CognitiveRigorEnglish_2.svg");
        rules.put("UseValueData", "true");
        rules.put("UseCountData", "false");    

        Chart_X1(dataGrid, rules);
                
        
    }


    private static void Chart_X1(DMemeGrid grid, Map<String, Object> rules) {
        
        SVGBase svgb = new SVGBase().Create();

        GridToChart graph = new GridToChart(svgb);
        graph.setRules(rules);
        graph.Build(grid);

        graph.ToFile("C:\\TROLLCREST_ROOT\\images", rules.get("OutFileName").toString());        
    }    
    
    
}
