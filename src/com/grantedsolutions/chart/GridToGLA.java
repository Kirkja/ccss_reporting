/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grantedsolutions.chart;

import com.learnerati.datameme.DMemeGrid;
import com.learnerati.datameme.DataMeme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author User1
 */
public class GridToGLA {

    private final Document doc;
    private final Element root;
    private final SVGBase base;

    private Double cellWidth = 50d;
    private Double cellHeight = 50d;
    private Double barHeight = 30d;
    private int gridInset = 0;

    private final Double horzOffset = 38d;
    private final Double vertOffset = 30d;

    private boolean useRowDescriptor = true;
    private boolean useColDescriptor = true;

    //private boolean _useCountData = true;
    //private boolean _useValueData = false;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> rules = new HashMap<>();
    private Map<String, Object> styles = new HashMap<>();

    public GridToGLA(SVGBase base) {
        doc = base.doc;
        root = base.doc.getDocumentElement();
        this.base = base;

        // default values
        params.put("vertical-anchor", "top");
        params.put("horizontal-anchor", "center");

        // default values
        styles.put("fill", "#3D5C00");
        styles.put("stroke", "#000000");
        styles.put("stroke-width", "0.25");
    }

    public void setRules(Map<String, Object> rulesMap) {
        rules = rulesMap;
    }

    public void setParams(Map<String, Object> paramMap) {
        params = paramMap;
    }

    public void setStyles(Map<String, Object> styleMap) {
        styles = styleMap;
    }

    
    private List<String> prefixSign(List<String> list) {
        List<String> prefixed = new ArrayList<>();
        
        for (String s : list) {
            if (new Integer(s) > 0) {
                prefixed.add("+" + s);
            }
            else {
                prefixed.add(s);
            }
        }
        
        return prefixed;
    }
    
    
    public void Build(DMemeGrid grid) {
        
        Double tickSpacing  = 50d;
        int rows            = grid.Rows();
        
        List<String> rowLabels = prefixSign(grid.getRowLabels());
        
        Double len      = (double)(rows * tickSpacing); 
        
        params.put("tickSpacing", tickSpacing);
        params.put("segments", rows);
        params.put("labels", rowLabels);
        
        base.CheckPoint(0d, 0d);
        
        // create the grid container -----------------------------------------
        Element gridContainer = doc.createElementNS(null, "g");
        gridContainer.setAttributeNS(null, "id", "grid_lines");
        gridContainer.setAttributeNS(null, "stroke", "black");
        gridContainer.setAttributeNS(null, "stroke-width", "0.5");
        gridContainer.setAttributeNS(null, "opacity", "1");

        Element barContainer = doc.createElementNS(null, "g");
        barContainer.setAttributeNS(null, "id", "grid_bars");
        barContainer.setAttributeNS(null, "stroke", "black");
        barContainer.setAttributeNS(null, "stroke-width", "0.25");
        
        barContainer.setAttributeNS(null, "opacity", "1");
        
        styles.put("fill", "orange");
        
        Double cellMaxWidth = 100d;        
        Double X1 = horzOffset;
        Double Y1 = vertOffset;

        params.put("horizontal-anchor", "left");
       
        for (int colIdx = 0; colIdx < grid.Cols(); colIdx++) {
            
            params.put("vertical-anchor", "top");
            X1 = horzOffset + (colIdx*cellMaxWidth) -5;
            
            gridContainer.appendChild(
                    new TickBar(doc).Create(
                            X1,
                            vertOffset,
                            len,
                            params
                    )
            );            
            
            Double colTotal = grid.getColTotal(colIdx);
            
            for (int rowIdx = 0; rowIdx < grid.Rows(); rowIdx++) {
                  
                Y1 = vertOffset + (rowIdx*tickSpacing)+tickSpacing/2d;
                
                params.put("vertical-anchor", "center");
                 
                DataMeme dm         = grid.getItem(rowIdx, colIdx);
                Double ratio        = (dm.asDouble() / colTotal);
                cellWidth           = ratio * cellMaxWidth;
                
                barContainer.appendChild(
                    new ShadedRect(doc).Create(
                            X1,
                            Y1,
                            cellWidth,
                            barHeight,
                            params,
                            styles
                    )
                );
                base.CheckPoint(X1, Y1);
                base.CheckPoint(X1 + cellWidth, Y1 + barHeight);
                                                
                if (cellWidth > 0d) {
                                        
                    Double x = cellWidth > 40
                            ? X1 +5d
                            : X1 +cellWidth +5d;
                                        
                    Double y = Y1 +3d;
                    
                    base.CheckPoint(x, y);
                    base.CheckPoint(x+20, y+30);
                  
                    
                    String cellLabel = String.format("%.0f", cellWidth);
                    Element label = doc.createElementNS(null, "text");
                    label.setAttributeNS(null, "text-anchor", "left");
                    label.setAttributeNS(null, "baseline-shift", "-33%");
                    label.setAttributeNS(null, "stroke", "none");
                    label.setAttributeNS(null, "fill", "black");
                    label.setAttributeNS(null, "font-size", "10pt");
                    label.setAttributeNS(null, "x", x.toString());
                    label.setAttributeNS(null, "y", y.toString());
                    label.appendChild(doc.createTextNode(
                            cellWidth > 0d
                            ? String.format("%s", cellLabel) + "%"
                            : String.format("%s", cellLabel)
                        )
                    );
                    barContainer.appendChild(label);                
                }

            }
        }
        
        root.appendChild(gridContainer);
        root.appendChild(barContainer);
    }

//=============================================================================
    public void ToFile(String path, String name) {
        base.WriteToFile(path, name);
    }

    public String ToString() {
        return base.WritetoString();
    }

}
