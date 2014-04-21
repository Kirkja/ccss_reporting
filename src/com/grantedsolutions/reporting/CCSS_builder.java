/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grantedsolutions.reporting;

import com.grantedsolutions.sql.DataManager;
import com.grantedsolutions.utilities.DocBuilder;
import com.grantedsolutions.utilities.ReadProperties;
import com.grantedsolutions.utilities.XMLBase;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 *
 * @author User1
 */
public class CCSS_builder {

    private static DataManager DM;
    protected Properties props;

    public CCSS_builder() {
        DM = new DataManager();

        try {
            DM.initPackage(DataManager.class, "database2.properties");
        } catch (Exception ex) {
            ;
        }

        props = new ReadProperties("output.properties").Fetch();
    }

    public void getProjectTree(DocBuilder report) {

        //DM.DisplayRecords(rs);
        Element section_I = report.CreateSection("Introduction");
        Element section_II = report.CreateSection("Brief Overview");
        Element section_III = report.CreateSection("Reading the Report");
        Element section_IV;// = report.CreateSection("Sorted Results");
        Element appendix = report.CreateSection("Appendices");

        section_I.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_Intro.txt"));
        section_II.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_Intro.txt"));
        section_III.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_Intro.txt"));
        //section_IV.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_Intro.txt"));

        Map<String, Element> mainResults = createCCSSResults(report);
        
        section_IV = mainResults.get("data");

        report.root.appendChild(section_I);
        report.root.appendChild(section_II);
        report.root.appendChild(section_III);
        report.root.appendChild(section_IV);
        report.root.appendChild(appendix);

    }

    private Map<String, Element> createCCSSResults(DocBuilder report) {
        Map<String, Element> results = new HashMap<>();

        String sql = "SELECT "
                + "  MSP.projectID "
                + ", MSP.siteID, BS.disname, BS.schname "
                + ", BC.gradeLevel "
                + ", BC.subjectArea "
                + "FROM `bank_collector` AS BC "
                + "LEFT JOIN `map_sample_collector` AS MSC ON MSC.collectorID = BC.id "
                + "LEFT JOIN map_collector_site MCS ON MCS.collectorID = MSC.collectorID "
                + "LEFT JOIN map_site_project MSP ON MSP.siteID = MCS.siteID "
                + "LEFT JOIN bank_site AS BS ON BS.id = MSP.siteID "
                + "WHERE BS.active = 'y' "
                + "AND MSP.active = 'y' "
                + "AND MCS.active = 'y' "
                + "AND MSC.active = 'y' "
                + "AND BC.active = 'y' "
                + "AND MSP.projectID = 23401520791814151  "
                + "GROUP BY BS.disname, BS.schname, BC.gradeLevel, BC.subjectArea "
                + "ORDER BY BS.disname, BS.schname, BC.gradeLevel, BC.subjectArea ";

        ResultSet rs = DM.Execute(sql);

        String curSiteID = "";
        String curGradeLevel = "";
        String curSubjectArea = "";

        boolean start = false;

        //Map<String, String> tree = new TreeMap<>();
        Element curSchool = null;
        Element curGrade = null;
        Element curSubject = null;
        
        Element holder = report.CreateSection("School Results");

        try {
            rs.beforeFirst();

            while (rs.next()) {
                
                
                // Initial NULL cases
                if (curSchool == null) {
                    curSiteID = rs.getString("siteID");
                    curSchool = report.CreateSection(rs.getString("schname"));
                    curSchool.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_filler.txt"));
                    System.out.println("\nSchool: " + rs.getString("schname"));                        
                }
                if (curGrade == null) {
                    curGradeLevel = rs.getString("gradeLevel");
                    curGrade = report.CreateSection("Grade " + rs.getString("gradeLevel"));
                    curGrade.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_filler.txt"));
                    System.out.println("\tGrade: " + rs.getString("gradeLevel"));                        
                }                    
                if (curSubject == null) {
                    curSubjectArea = rs.getString("subjectArea");
                    curSubject = report.CreateSection(rs.getString("subjectArea"));
                    curSubject.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_filler.txt"));
                    System.out.println("\tSubject: " + rs.getString("subjectArea"));                        
                }                 
                
                // Running checks
                if (!curSubjectArea.equals(rs.getString("subjectArea"))) {

                    curGrade.appendChild(curSubject);
                    
                    curSubject = report.CreateSection(rs.getString("subjectArea"));
                    curSubject.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_filler.txt"));

                    System.out.println("\t\tSubject area: " + rs.getString("subjectArea"));                    
                }
                 
                if (!curGradeLevel.equals(rs.getString("gradeLevel"))) {

                    curGrade.appendChild(curSubject);
                    curSchool.appendChild(curGrade);
                    
                    curGrade = report.CreateSection("Grade " + rs.getString("gradeLevel"));
                    curGrade.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_filler.txt"));

                    System.out.println("\tGrade level: " + rs.getString("gradeLevel"));
                }               
                
                if (!curSiteID.equals(rs.getString("siteID"))) {

                    curGrade.appendChild(curSubject);
                    curSchool.appendChild(curGrade);
                    holder.appendChild(curSchool);
                    
                    curSchool = report.CreateSection(rs.getString("schname"));
                    curSchool.appendChild(report.PullExternal(props.getProperty("file.inbase") + "insert_files/CCSS_filler.txt"));

                    curSchool.setAttribute("bump", "true");
                    System.out.println("\nSchool: " + rs.getString("schname"));                    
                }





                curSiteID = rs.getString("siteID");
                curGradeLevel = rs.getString("gradeLevel");
                curSubjectArea = rs.getString("subjectArea");

            }
            
            curGrade.appendChild(curSubject);
            curSchool.appendChild(curGrade);
            holder.appendChild(curSchool);

        } catch (SQLException ex) {
            Logger.getLogger(CCSS_builder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        results.put("data", holder);
        //report.base.root.appendChild(holder);

        return results;
    }

}