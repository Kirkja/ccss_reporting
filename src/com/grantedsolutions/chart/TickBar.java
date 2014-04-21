/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grantedsolutions.chart;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author User1
 */
public class TickBar {

    private static DocumentFragment frag;
    private static Document doc;
    private static Map<String, Object> _params;
    private static Map<String, Object> _attrs;
    private static Map<String, Object> _style;

    /**
     *
     * @param styles A HashMap containing styles and their values.
     */
    protected void SetStyles(Map<String, Object> styles) {
        _style = styles;
    }

    /**
     *
     * @param params A HashMap containing parameters and their values.
     */
    protected void SetParams(Map<String, Object> params) {
        _params = params;
    }

    /**
     *
     * @param doc The Document doc used for creating Elements against.
     */
    public TickBar(Document doc) {
        TickBar.doc = doc;
        TickBar.frag = doc.createDocumentFragment();

        _params = new HashMap<>();
        _attrs = new HashMap<>();
        _style = new HashMap<>();
    }

    public Node Create(double x, 
            double y, 
            double len) {
        
        return Create(x, y, len, "");
    }

    public Node Create(double x, double y, double len, String title) {
        
        double w = 10d;
        
        double cx = x - w / 2;
        double cy = y - len / 2;

        if (_params != null) {
            if (_params.containsKey("vertical-anchor")) {
                switch (_params.get("vertical-anchor").toString()) {
                    case "top":
                        cy = y;
                        break;

                    case "bottom":
                        cy = cy - len;
                        break;

                    case "center":
                        // default case
                        break;
                }
            }

            if (_params.containsKey("horizontal-anchor")) {
                switch (_params.get("horizontal-anchor").toString()) {
                    case "left":
                        cx = x;
                        break;

                    case "right":
                        cx = x - w;
                        break;

                    case "center":
                        // default case
                        break;
                }
            }
        }

        
        Element elem = doc.createElement("line");
        elem.setAttribute("x1", String.format("%.4f", cx));
        elem.setAttribute("y1", String.format("%.4f", cy));
        
        elem.setAttribute("x2", String.format("%.4f", cx));
        elem.setAttribute("y2", String.format("%.4f", cy + len));

        if (_style != null) {
            for (Iterator iter = _style.keySet().iterator(); iter.hasNext();) {
                String name = iter.next().toString();
                Object value = _style.get(name);

                elem.setAttributeNS(null, name, value.toString());
            }
        }

        // sets up up a tooltip for capable viewers
        if (!title.isEmpty()) {
            Element tooltip = doc.createElement("title");
            tooltip.appendChild(doc.createTextNode(title));
            elem.appendChild(tooltip);
        }

        frag.appendChild(elem);

        return frag;
    }

}
