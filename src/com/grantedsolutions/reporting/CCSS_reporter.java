/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grantedsolutions.reporting;

import com.grantedsolutions.sql.DataManager;
import com.learnerati.datameme.DMemeGrid;
import com.learnerati.datameme.DataMeme;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author User1
 */
public class CCSS_reporter {

    private static DataManager DM;

    public CCSS_reporter() {
        DM = new DataManager();

        try {
            DM.initPackage(DataManager.class, "database2.properties");
        } catch (Exception ex) {
            ;
        }
    }

    public void accessTree() {

        String sql = "SELECT \n"
                + "  BP.accountID, MSP.projectID, MSP.siteID, BP.name AS projectName\n"
                + ", BSite.disname AS districtName, BSite.schname AS schoolName\n"
                + ", MCSite.collectorID\n"
                + ", BC.subjectArea, BC.gradeLevel\n"
                + ", MSC.sampleID\n"
                + ", RD.groupingID, RD.dataName, RD.dataValue\n"
                + "FROM `bank_project` AS BP\n"
                + "LEFT JOIN map_site_project AS MSP ON MSP.projectID = BP.id\n"
                + "LEFT JOIN bank_site AS BSite ON BSite.id = MSP.siteID\n"
                + "LEFT JOIN map_collector_site AS MCSite ON MCSite.siteID = MSP.siteID\n"
                + "LEFT JOIN bank_collector AS BC ON BC.id = MCSite.collectorID\n"
                + "LEFT JOIN map_sample_collector AS MSC ON MSC.collectorID = MCSite.collectorID\n"
                + "LEFT JOIN review_data AS RD ON RD.sampleID = MSC.sampleID\n"
                + "WHERE MSP.projectID = 23401520791814151\n"
                + "AND MSP.active = 'y'\n"
                + "AND BSite.active = 'y'\n"
                + "AND MCSite.active = 'y'\n"
                + "AND BC.active = 'y'\n"
                + "AND MSC.active = 'y'\n"
                + "AND RD.active = 'y'\n"
                + "AND RD.dataName IN ('standard', 'dok', 'blm','counter')\n"
                + "AND RD.dataValue <> \"\"\n"
                + "AND BC.subjectArea = 'English'\n"
                + "AND BC.gradeLevel = 2\n"
                + "ORDER BY BP.name, BSite.disname, BSite.schname, \n"
                + "BC.gradeLevel, BC.subjectArea, MSC.sampleID, \n"
                + "RD.groupingID, RD.dataName, RD.dataValue  ";
        //+ "LIMIT 5000";

        ResultSet rs = DM.Execute(sql);

        try {
            rs.beforeFirst();
            //ResultSetMetaData metaData = rs.getMetaData();

            String currentCollectorID = new String();
            String currentSampleID = new String();
            int currentGroupingID = 0;
            String currentDOK = new String();
            String currentBLM = new String();

            Boolean jump = false;

            Map<String, String> item = new HashMap<>();
            Map<String, Integer> sample = new HashMap<>();

            DMemeGrid dmg = new DMemeGrid();

            dmg.setLabel("Cognitive Rigor: English Grade 2");
            dmg.setColDescriptor("This the column descriptor here");
            dmg.setRowDescriptor("This is the row descriptor here");

            dmg.addRowLabel("DOK-4");
            dmg.addRowLabel("DOK-3");
            dmg.addRowLabel("DOK-2");
            dmg.addRowLabel("DOK-1");
            dmg.addColLabel("BLM-1");
            dmg.addColLabel("BLM-2");
            dmg.addColLabel("BLM-3");
            dmg.addColLabel("BLM-4");
            dmg.addColLabel("BLM-5");
            dmg.addColLabel("BLM-6");
           
            while (rs.next()) {

                if (currentGroupingID > 0) {
                    if (rs.getString("collectorID") != currentCollectorID) {
                        //currentCollectorID = rs.getString("collectorID");
                        //jump = true;
                    }

                    if (rs.getString("sampleID") != currentSampleID) {
                        //currentSampleID = rs.getString("sampleID");
                        //jump = true;
                    }

                    if (rs.getInt("groupingID") != currentGroupingID) {
                        //currentGroupingID = rs.getString("groupingID");                        
                        jump = true;
                    }
                }

                if (jump == true) {

                    String CR = String.format("%s:%s", item.get("dok"), item.get("blm"));

                    String[] rigor = CR.split(":");
                    int dok = new Integer(rigor[0].replace("DOK-", ""));
                    int blm = new Integer(rigor[1].replace("BLM-", ""));
                    
                    
                    
                    if (dmg.hasElement(4-dok, blm-1)) {
                        int c = new Integer(item.get("counter"));
                        
                        int c2 = dmg.getItem(4-dok, blm-1).isNumeric() 
                                ? dmg.getItem(4-dok, blm-1).asInteger() 
                                : 0;                                                
                        
                        dmg.addItem(4-dok, blm-1, new DataMeme(c+c2));
                    }
                    else {
                        dmg.addItem(4-dok, blm-1, new DataMeme(new Integer(item.get("counter"))));
                    }


                    if (sample.containsKey(CR)) {
                        Integer c = new Integer(item.get("counter"));
                        Integer c2 = sample.get(CR);
                        sample.put(CR, c + c2);
                    } else {
                        sample.put(CR, new Integer(item.get("counter")));
                    }

                    item.clear();
                    jump = false;
                }

                item.put(rs.getString("dataName"), rs.getString("dataValue"));

                currentCollectorID = rs.getString("collectorID");
                currentSampleID = rs.getString("sampleID");
                currentGroupingID = rs.getInt("groupingID");

            }
            System.out.println("");

            displayMap(sample);

            dmg.DumpGrid();

        } catch (SQLException ex) {
            Logger.getLogger(CCSS_reporter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    private void displayMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.print("\n" + pairs.getKey() + " = " + pairs.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
    public DMemeGrid pullRigorData(
            String projectID, 
            String siteID, 
            String subjectArea, 
            String gradeLevel) {
        
        DMemeGrid dmg = new DMemeGrid();
        
        dmg.setLabel(String.format("Cognitive Rigor: %s Grade %s", subjectArea, gradeLevel));
        dmg.setColDescriptor("Bloom's Taxonomy (Revised)");
        dmg.setRowDescriptor("Depth of Knowledge");

        dmg.addRowLabel("4");
        dmg.addRowLabel("3");
        dmg.addRowLabel("2");
        dmg.addRowLabel("1");
        dmg.addColLabel("1");
        dmg.addColLabel("2");
        dmg.addColLabel("3");
        dmg.addColLabel("4");
        dmg.addColLabel("5");
        dmg.addColLabel("6");        
        
        
        return dmg;
    }
    
    
    
    
    public DMemeGrid getRigorData() {
        
        DMemeGrid dmg = new DMemeGrid();
                  
        String sql = "SELECT \n"
                + "  BP.accountID, MSP.projectID, MSP.siteID, BP.name AS projectName\n"
                + ", BSite.disname AS districtName, BSite.schname AS schoolName\n"
                + ", MCSite.collectorID\n"
                + ", BC.subjectArea, BC.gradeLevel\n"
                + ", MSC.sampleID\n"
                + ", RD.groupingID, RD.dataName, RD.dataValue\n"
                + "FROM `bank_project` AS BP\n"
                + "LEFT JOIN map_site_project AS MSP ON MSP.projectID = BP.id\n"
                + "LEFT JOIN bank_site AS BSite ON BSite.id = MSP.siteID\n"
                + "LEFT JOIN map_collector_site AS MCSite ON MCSite.siteID = MSP.siteID\n"
                + "LEFT JOIN bank_collector AS BC ON BC.id = MCSite.collectorID\n"
                + "LEFT JOIN map_sample_collector AS MSC ON MSC.collectorID = MCSite.collectorID\n"
                + "LEFT JOIN review_data AS RD ON RD.sampleID = MSC.sampleID\n"
                + "WHERE MSP.projectID = 23401520791814151\n"
                + "AND MSP.active = 'y'\n"
                + "AND BSite.active = 'y'\n"
                + "AND MCSite.active = 'y'\n"
                + "AND BC.active = 'y'\n"
                + "AND MSC.active = 'y'\n"
                + "AND RD.active = 'y'\n"
                + "AND RD.dataName IN ('standard', 'dok', 'blm','counter')\n"
                + "AND RD.dataValue <> \"\"\n"
                + "AND BC.subjectArea = 'English'\n"
                + "AND BC.gradeLevel = 2\n"
                + "ORDER BY BP.name, BSite.disname, BSite.schname, \n"
                + "BC.gradeLevel, BC.subjectArea, MSC.sampleID, \n"
                + "RD.groupingID, RD.dataName, RD.dataValue  ";
        

        ResultSet rs = DM.Execute(sql);

        try {
            rs.beforeFirst();
        
            String currentCollectorID = new String();
            String currentSampleID = new String();
            int currentGroupingID = 0;
            String currentDOK = new String();
            String currentBLM = new String();

            Boolean jump = false;

            Map<String, String> item    = new HashMap<>();
            Map<String, Integer> sample = new TreeMap<>();
           
            dmg.setLabel("Cognitive Rigor: English Grade 2");
            dmg.setColDescriptor("Bloom's Taxonomy (Revised)");
            dmg.setRowDescriptor("Depth of Knowledge");

            dmg.addRowLabel("4");
            dmg.addRowLabel("3");
            dmg.addRowLabel("2");
            dmg.addRowLabel("1");
            dmg.addColLabel("1");
            dmg.addColLabel("2");
            dmg.addColLabel("3");
            dmg.addColLabel("4");
            dmg.addColLabel("5");
            dmg.addColLabel("6");
           
            while (rs.next()) {

                if (currentGroupingID > 0) {

                    if (rs.getInt("groupingID") != currentGroupingID) {
                        //currentGroupingID = rs.getString("groupingID");                        
                        jump = true;
                    }
                }

                if (jump == true) {

                    String CR = String.format("%s:%s", item.get("dok"), item.get("blm"));

                    String[] rigor = CR.split(":");
                    
                    int dok = new Integer(rigor[0].replace("DOK-", ""));
                    int blm = new Integer(rigor[1].replace("BLM-", ""));
                                                            
                    if (dmg.hasElement(4-dok, blm-1)) {
                        int c = new Integer(item.get("counter"));
                        
                        int c2 = dmg.getItem(4-dok, blm-1).isNumeric() 
                                ? dmg.getItem(4-dok, blm-1).asInteger() 
                                : 0;                                                
                        
                        dmg.addItem(4-dok, blm-1, new DataMeme(c+c2));
                    }
                    else {
                        dmg.addItem(4-dok, blm-1, new DataMeme(new Integer(item.get("counter"))));
                    }

                    
                    item.clear();
                    jump = false;
                }

                item.put(rs.getString("dataName"), rs.getString("dataValue"));

                currentCollectorID = rs.getString("collectorID");
                currentSampleID = rs.getString("sampleID");
                currentGroupingID = rs.getInt("groupingID");

            }

        } catch (SQLException ex) {
            Logger.getLogger(CCSS_reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dmg;
    }
    
    
    public DMemeGrid getStandardData() {
        
        DMemeGrid dmg = new DMemeGrid();
        
        dmg.setLabel("Standards: English Grade 2");
        //dmg.setColDescriptor("This the column descriptor here");
        //dmg.setRowDescriptor("This is the row descriptor here");
        
        dmg.setColDescriptor("Collected Content Areas");
        dmg.setRowDescriptor("Drift in Grade Level");   
            
        Integer total = 0;
                    
        String sql = "SELECT \n" +
                    "  MSP.projectID\n" +
                    ", BP.name AS projectName\n" +
                    ", BSite.disname AS districtName\n" +
                    ", BSite.schname AS schoolName\n" +
                    ", MSP.siteID\n" +
                    ", MCSite.collectorID\n" +
                    ", MSC.sampleID\n" +
                    ", BC.gradeLevel, BC.subjectArea\n" +
                    ", RD.groupingID, RD.dataName, RD.dataValue, RD.dataType\n" +
                    "FROM map_site_project AS MSP\n" +
                    "LEFT JOIN bank_site 		AS BSite 	ON BSite.id =  MSP.siteID\n" +
                    "LEFT JOIN bank_project		AS BP		ON BP.id = MSP.projectID\n" +
                    "LEFT JOIN map_collector_site 	AS MCSite 	ON MCSite.siteID = MSP.siteID\n" +
                    "LEFT JOIN map_sample_collector 	AS MSC 		ON MSC.collectorID = MCSite.collectorID\n" +
                    "LEFT JOIN review_data		AS RD 		ON (\n" +
                    "   RD.sampleID = MSC.sampleID\n" +
                    "   AND RD.projectID = MSP.projectID\n" +
                    ")\n" +
                    "LEFT JOIN bank_collector 	AS BC 		ON BC.id = MSC.collectorID\n" +
                    "WHERE MSC.sampleID IS NOT NULL\n" +
                    "AND MSP.active = 'y'\n" +
                    "AND MCSite.active = 'y'\n" +
                    "AND BC.active = 'y'\n" +
                    "AND MSC.active = 'y'\n" +
                    "AND RD.active = 'y'\n" +
                    "AND RD.dataValue <> ''\n" +
                    "AND MSP.projectID = 1\n" +
                    "AND BC.gradeLevel = 5\n" +
                    //"AND BC.subjectArea = 'Mathematics'\n" +
                    "AND RD.dataName IN ('standard', 'counter')\n" +
                    "ORDER BY BP.name, BSite.disname, BSite.schname, \n" +
                    "BC.gradeLevel, BC.subjectArea, MSC.sampleID, \n" +
                    "RD.groupingID, RD.dataName, RD.dataValue ";
            
            

        ResultSet rs = DM.Execute(sql);
        
            Map<String, String> item    = new HashMap<>();
            Map<Integer, Integer> sample = new TreeMap<>(Collections.reverseOrder());
                         
            Map<String, Map<Integer, Integer>> sampleB = new TreeMap<>();
                                                 
        try {
            rs.beforeFirst();
            
            String currentCollectorID   = new String();
            String currentSampleID      = new String();
            String currentStandard      = new String(); 
            String currentSubjectArea   = new String();
            int currentGroupingID       = 0;  
            int currentGradeLevel       = 0;
            Boolean jump                = false;

                    
            while (rs.next()) {
                if (currentGroupingID > 0) {

                    if (rs.getInt("groupingID") != currentGroupingID) {
                        //currentGroupingID = rs.getString("groupingID");                        
                        jump = true;
                    }
                }
                
                if (jump == true) {

                    String stnd     = item.get("standard");
                    Integer hits    = new Integer(item.get("counter"));                    
                    String[] sagl   = stnd.split("_")[1].split("\\.");
                                         
                    total += hits;
                    
                    Integer agl = 0;
                    
                    if (sagl[0].equals("K")) {
                        ; //
                    } else {
                        agl = new Integer(sagl[0]);
                    }
                    
                    Integer drift = agl -currentGradeLevel;
                    
                    /*
                    if (sample.containsKey(drift)) {
                        Integer c = sample.get(drift);
                        sample.put(drift, c + hits);
                    }
                    else {
                        sample.put(drift, hits);
                    }
                    */
                    
                    if(sampleB.containsKey(currentSubjectArea)) {
                        if (sampleB.get(currentSubjectArea).containsKey(drift)) {
                            Integer c = sampleB.get(currentSubjectArea).get(drift);
                            sampleB.get(currentSubjectArea).put(drift, c + hits);
                        }
                        else {
                            sampleB.get(currentSubjectArea).put(drift, hits);
                        }
                    } else {
                        Map<Integer, Integer> dmap = new TreeMap<>(Collections.reverseOrder());
                        dmap.put(drift, hits);
                        sampleB.put(currentSubjectArea, dmap);
                    }

                    item.clear();
                    jump = false;
                }

                item.put(rs.getString("dataName"), rs.getString("dataValue"));

                currentCollectorID  = rs.getString("collectorID");
                currentSampleID     = rs.getString("sampleID");
                currentGroupingID   = rs.getInt("groupingID");               
                currentGradeLevel   = rs.getInt("gradeLevel");
                currentSubjectArea  = rs.getString("subjectArea");
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(CCSS_reporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //displayMap(sampleB);
        
        //dmg = convertToGrid(sample);
        
        DMemeGrid g2 = convertToGridB(sampleB);
        //g2.DumpGrid();
        
        System.out.println("\nTotal = " + total);
        
        return g2;
    }
    
    
    private DMemeGrid convertToGrid(Map mp) {
        
        DMemeGrid grid = new DMemeGrid();
        
        int rowIdx = 0;
        int colIdx = 0;
        
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            
            grid.addRowLabel(rowIdx, pairs.getKey().toString());
            grid.addItem(rowIdx, colIdx, new DataMeme(pairs.getValue()));

            rowIdx++;
        }
        
        return grid;
    }

    
    private DMemeGrid convertToGridB(Map mp) {
        
        DMemeGrid grid = new DMemeGrid();
        
        int rowIdx = 0;
        int colIdx = 0;
        
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            
            Map<Integer, Integer> tmp = (TreeMap<Integer,Integer>)pairs.getValue();

            grid.addColLabel(pairs.getKey().toString());
            
            Iterator it2 = tmp.entrySet().iterator();
            rowIdx = 0;
            while (it2.hasNext()) {
               Map.Entry item = (Map.Entry) it2.next();
               
               Integer i = new Integer(item.getKey().toString());
               String label = item.getKey().toString();
               
               if (i > 0) {
                   label = String.format("+%d", i);
               }
               
               if (!grid.getRowLabels().contains(label)) {
                   grid.addRowLabel(label);
               }
                
               grid.addItem(rowIdx, colIdx, new DataMeme(item.getValue()));
               
               rowIdx++; 
            }
           colIdx++; 
        }
        
        
        
        return grid;
    }
    
    
    
    
}
