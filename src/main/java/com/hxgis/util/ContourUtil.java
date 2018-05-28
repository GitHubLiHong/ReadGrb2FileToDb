package com.hxgis.util;

import wContour.Contour;
import wContour.Global.Border;
import wContour.Global.PointD;
import wContour.Global.PolyLine;
import wContour.Global.Polygon;
import wContour.Interpolate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lm on 2017/5/26.
 */

public class ContourUtil {

    public static List<wContour.Global.Polygon> genCountourLines(double[] xs,
                                                                 double[] ys, double[][] values, double[] contourValues, int xNum, int yNum, double undefValue) {
        int nc = contourValues.length;
        int[][] S1 = new int[yNum][xNum];
        List<Border> borders = Contour
                .tracingBorders(values, xs, ys, S1, undefValue);
        List<PolyLine> contourLines = Contour.tracingContourLines(values, xs, ys, nc,
                contourValues, undefValue, borders, S1);
        List<wContour.Global.Polygon> contourPolygons = Contour.tracingPolygons(values, contourLines,
                borders, contourValues);
        return contourPolygons;
    }
}
