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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

                    /*
                     System.out.printf("\n%s \t%s \t%s \t%s \t%s",
                     currentCollectorID, currentSampleID, currentGroupingID,
                     CR, item.get("counter")
                     );
                     */
                    
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

}
