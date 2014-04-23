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
        
        // Draw a raw CR Chart
        //Test_C();
        
        
        // Draw a raw standards chart
        Test_D();
        
        // Build out a project tree for reporting
        //Test_E();
        
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
        
        DMemeGrid dataGrid = reporter.getRigorData();
        
        dataGrid.DumpGrid();
        
        // define some rules to use for the chart
        Map<String,Object> rules = new HashMap<>();                
        rules.put("OutFileName", "CognitiveRigorEnglish_2.svg");
        rules.put("UseValueData", "true");
        rules.put("UseCountData", "false");    

        Chart_X1(dataGrid, rules);                        
    }

    private static void Test_D() {
        
        CCSS_reporter reporter = new CCSS_reporter();
        
        DMemeGrid dataGrid = reporter.getStandardData();
        dataGrid.addColLabel("SCI");
        
        dataGrid.setRowDescriptor("Grade Level Drift");
        dataGrid.setColDescriptor("Content Areas");
        
        DMemeList colB = dataGrid.copyColumn(0);
        
        
        //colB.setLabel("ELA");        
        dataGrid.addColumn(colB);
        dataGrid.addColLabel("ELA");
        
        //colB.setLabel("Math");
        dataGrid.addColumn(colB);
        dataGrid.addColLabel("MTH");
        
        //colB.setLabel("SCI");
        colB.Reverse();
        
        //colB.setLabel("SOC");
        dataGrid.addColumn(colB);
        dataGrid.addColLabel("SOC");
        
        dataGrid.DumpGrid();
        
        // define some rules to use for the chart
        Map<String,Object> rules = new HashMap<>();                
        rules.put("OutFileName", "StandardsEnglish_2.svg");
        rules.put("UseValueData", "true");
        rules.put("UseCountData", "false");    

        Chart_X2(dataGrid, rules); 
        
        
    }

    
    private static void Test_E() {
                
        Map<String, String> configOptions = new HashMap<>();
        configOptions.put("base-image-directory",   "c:/GS_ROOT/images/");
        configOptions.put("base-font-directory",    "c:/GS_ROOT/fonts/");
        configOptions.put("base-doc-directory",     "c:/GS_ROOT/docs/");
        configOptions.put("number-of-columns",      "1");        
        
        Map<String, String> coverOptions = new HashMap<>();
        coverOptions.put("title-A", "title A here");
        coverOptions.put("title-B", "title B here");
        coverOptions.put("title-C", "SCR Analysis");        
        coverOptions.put("title-sub", "title sub here");
        coverOptions.put("published-by", "published by here");
        
        Map<String, String> abstractOptions = new HashMap<>();
        abstractOptions.put("title", "abstract title here");
        abstractOptions.put("body", "abstract body here");
        
               
        XMLBase xmlBase = new XMLBase().Create("document");

        DocBuilder dataReport = new DocBuilder(xmlBase);
        dataReport.SetConfig(configOptions);
        dataReport.SetCover(coverOptions);
        dataReport.SetAbstract(abstractOptions);        
                
        CCSS_builder builder = new CCSS_builder();
        
        builder.getProjectTree(dataReport);
        
        xmlBase.WriteXML("ccss_report.xml");
    }
    

    private static void Chart_X1(DMemeGrid grid, Map<String, Object> rules) {
        
        SVGBase svgb = new SVGBase().Create();

        GridToChart graph = new GridToChart(svgb);
        graph.setRules(rules);
        graph.Build(grid);

        graph.ToFile("C:\\TROLLCREST_ROOT\\images", rules.get("OutFileName").toString());        
    }   
    
    private static void Chart_X2(DMemeGrid grid, Map<String, Object> rules) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("vertical-anchor", "top");
        
        SVGBase svgb = new SVGBase().Create();

        GridToGLA graph = new GridToGLA(svgb);
        graph.setRules(rules);        
        graph.Build(grid);

        graph.ToFile("C:\\TROLLCREST_ROOT\\images", rules.get("OutFileName").toString());        
    }        
    
    
}
