package org.phnq.core.util.geometry;

import java.util.ArrayList;

/**
 *
 * @author pgostovic
 */
public class Polygon extends ArrayList<Point> {

    public static Polygon parsePolygon(String polyStr) {
        String[] comps = polyStr.split(",");
        Polygon polygon = new Polygon(comps.length);
        if (comps.length % 2 == 0 && comps.length >= 6) {
            for (int i = 0; i < comps.length; i += 2) {
                polygon.add(new Point(Double.parseDouble(comps[i]), Double.parseDouble(comps[i + 1])));
            }
        } else {
            throw new IllegalArgumentException("Polygon must have an even number of components.");
        }
        return polygon;
    }

    public Polygon(int i) {
        super(i);
    }
}
