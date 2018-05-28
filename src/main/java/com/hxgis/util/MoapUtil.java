package com.hxgis.util;

import com.hxgis.common.Constant;
import com.hxgis.model.MoapEntity.Feature;
import com.hxgis.model.MoapEntity.FeatureCollection;
import com.hxgis.model.MoapEntity.FeatureJson;
import com.hxgis.model.geojson.GeoFeature;
import com.hxgis.model.geojson.GeoFeatureCollection;
import com.hxgis.model.geojson.geometry.GeoGeometryPolygon;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.util.StringUtils;
import wContour.Global.PointD;
import wContour.Global.PolyLine;
import wContour.Global.Polygon;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

//import wContour.Global.PointD;
//import wContour.Global.PolyLine;

//import org.apache.avro.generic.GenericData;

/**
 * Created by lm on 2017/6/8.
 */
public class MoapUtil {

    public static Properties prop = PropertiesUtils
            .loadProperties("classpath:grb2timer.properties");
    public static Map<String, String> relateMap = new HashMap<String, String>() {{
        put("ER03", "pre3hSbtVal");
        put("TMP", "temp24hSbtVal");
        put("TMAX", "temp24hSbtVal");
        put("TMIN", "temp24hSbtVal");
        put("ERH", "erhSbtVal");
        put("ECT", "ectSbtVal");
        put("VIS", "visbilitySbtVal");
        put("EDA10", "eda10SbtVal");
        put("PPH", "pphSbtVal");
        put("FOG", "fogSbtVal");
        put("HZ", "hzSbtVal");
        put("SAND", "sandSbtVal");
        put("SSM", "ssmSbtVal");
        put("HAIL", "hailSbtVal");
        put("WP3", "wp3SbtVal");
    }};


    public static List<Polygon> disCrete(double[] xs, double[] ys, double[][] valueArray, int xNum, int yNum, double[] contourValues) {
        List<Polygon> list = null;
        try {
            list = ContourUtil.genCountourLines(xs, ys, valueArray, contourValues, xNum, yNum, 999990);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 将Grb数据转换成 moapFeatureJson  和  普通的geoJson
     *
     * @param xs
     * @param ys
     * @param valueArray
     * @param eleType    要素类型
     * @param bound      地图裁剪边界   @return
     */
    public static Map<String, Object> dataToPolygonJson(double[] xs, double[] ys, double[][] valueArray, String eleType, String bound) {
        Map<String, Object> map = new HashMap<>();
        FeatureJson featureJson = new FeatureJson();
        featureJson.setStatus("1");
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setType("FeatureCollection");
        Properties properties = new Properties();
        featureCollection.setProperties(properties);
        Feature feature = new Feature();
        String[] field = new String[]{eleType};
        String[] filedType = new String[]{"double"};
        feature.setField(field);
        feature.setFieldType(filedType);
        feature.setGeoType("Polygon");
        StringBuffer sb = new StringBuffer();
        String typeValue = relateMap.get(eleType.toUpperCase());
        String sbvalue = prop.getProperty(typeValue);
        double[] contourValues = new double[sbvalue.split(",").length];
        for (int i = 0; i < sbvalue.split(",").length; i++) {
            contourValues[i] = Double.parseDouble(sbvalue.split(",")[i]);
        }
        GeoFeatureCollection geoFeatureCollection = new GeoFeatureCollection();
        List<GeoFeature> geoFeatures = new ArrayList<>();
        List<Polygon> list = disCrete(xs, ys, valueArray, Constant.GRB2_XSIZE, Constant.GRB2_YSIZE, contourValues);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Polygon polygon = list.get(i);
                PolyLine polyLine = polygon.OutLine;
                int number = polyLine.PointList.size();

                //geoJson
                GeoGeometryPolygon geoPolygon = new GeoGeometryPolygon();
                List<List<double[]>> coordinates = new ArrayList<>();
                List<double[]> coordinate = new ArrayList<>();

                for (int m = 0; m < number; m++) {
                    PointD pointD = polyLine.PointList.get(m);
                    pointD.X = new BigDecimal(pointD.X).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                    pointD.Y = new BigDecimal(pointD.Y).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                    sb.append(pointD.X + " ");
                    if (m != number - 1) {
                        sb.append(pointD.Y + " ");
                    } else {
                        sb.append(pointD.Y + ",");
                    }
                    double[] d = {pointD.X, pointD.Y};
                    coordinate.add(d);
                }
                coordinates.add(coordinate);
                geoPolygon.setCoordinates(coordinates);
                GeoFeature geoFeature = new GeoFeature(geoPolygon);
                double pvalue = polygon.LowValue;
                Color color = getColor(pvalue, eleType);
                geoFeature.getProperties().put("color", color);
                geoFeatures.add(geoFeature);
//                int index = indexOfArray(contourValues, pvalue);
//                if (index < contourValues.length - 1 && index > 0) {
//                    pvalue = contourValues[indexOfArray(contourValues, pvalue) - 1];
//                }
//                }
                sb.append(pvalue);
                sb.append(";");
            }
        }
        String resultData = sb.toString();
        if (resultData.contains(";"))
            resultData = resultData.substring(0, resultData.length() - 1);
        feature.setData(resultData);
        featureCollection.setFeature(feature);
        featureJson.setData(featureCollection);

        geoFeatureCollection.setFeatures(geoFeatures);

        map.put("featureJson", featureJson);
        map.put("geoJson", geoFeatureCollection);
        return map;
    }

    private static Color getColor(double pvalue, String eleType) {
        int r = 255, g = 255, b = 255, a = 255;
        if ("ER03".equals(eleType)) {
            if (Double.compare(pvalue, 0.0) == 0) {
                r = 255;
                g = 255;
                b = 255;
                a = 0;
            } else if (Double.compare(pvalue, 0.1) == 0) {
                r = 165;
                g = 243;
                b = 141;
                a = 127;
            } else if (Double.compare(pvalue, 0.3) == 0) {
                r = 61;
                g = 185;
                b = 63;
                a = 127;
            } else if (Double.compare(pvalue, 10.0) == 0) {
                r = 99;
                g = 184;
                b = 249;
                a = 127;
            } else if (Double.compare(pvalue, 20.0) == 0) {
                r = 0;
                g = 0;
                b = 254;
                a = 127;
            } else if (Double.compare(pvalue, 50.0) == 0) {
                r = 243;
                g = 5;
                b = 238;
                a = 127;
            } else if (Double.compare(pvalue, 70.0) == 0) {
                r = 129;
                g = 0;
                b = 64;
                a = 127;
            }
        } else if ("TMP".equals(eleType) || "TMAX".equals(eleType) || "TMIN".equals(eleType)) {
            if (Double.compare(pvalue, -20.0) == 0) {
                r = 8;
                g = 8;
                b = 247;
                a = 172;
            } else if (Double.compare(pvalue, -12.0) == 0) {
                r = 8;
                g = 6;
                b = 239;
                a = 172;
            } else if (Double.compare(pvalue, -8.0) == 0) {
                r = 8;
                g = 101;
                b = 239;
                a = 172;
            } else if (Double.compare(pvalue, -6.0) == 0) {
                r = 8;
                g = 130;
                b = 247;
                a = 172;
            } else if (Double.compare(pvalue, -4.0) == 0) {
                r = 8;
                g = 162;
                b = 247;
                a = 172;
            } else if (Double.compare(pvalue, -2.0) == 0) {
                r = 8;
                g = 190;
                b = 239;
                a = 172;
            } else if (Double.compare(pvalue, 0.0) == 0) {
                r = 8;
                g = 219;
                b = 239;
                a = 172;
            } else if (Double.compare(pvalue, 2.0) == 0) {
                r = 24;
                g = 255;
                b = 255;
                a = 172;
            } else if (Double.compare(pvalue, 4.0) == 0) {
                r = 24;
                g = 255;
                b = 189;
                a = 172;
            } else if (Double.compare(pvalue, 6.0) == 0) {
                r = 24;
                g = 255;
                b = 132;
                a = 172;
            } else if (Double.compare(pvalue, 8.0) == 0) {
                r = 24;
                g = 255;
                b = 66;
                a = 172;
            } else if (Double.compare(pvalue, 12.0) == 0) {
                r = 132;
                g = 255;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 16.0) == 0) {
                r = 156;
                g = 255;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 20.0) == 0) {
                r = 189;
                g = 255;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 24.0) == 0) {
                r = 222;
                g = 255;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 26.0) == 0) {
                r = 255;
                g = 231;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 28.0) == 0) {
                r = 255;
                g = 203;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 30.0) == 0) {
                r = 255;
                g = 170;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 32.0) == 0) {
                r = 255;
                g = 142;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 35.0) == 0) {
                r = 255;
                g = 113;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 37.0) == 0) {
                r = 255;
                g = 81;
                b = 24;
                a = 172;
            } else if (Double.compare(pvalue, 38.0) == 0) {
                r = 255;
                g = 60;
                b = 24;
                a = 172;
            }
        } else if ("EDA10".equals(eleType)) {
            if (Double.compare(pvalue, 1.6) == 0) {
                r = 89;
                g = 254;
                b = 4;
                a = 255;
            } else if (Double.compare(pvalue, 3.4) == 0) {
                r = 213;
                g = 253;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 5.5) == 0) {
                r = 255;
                g = 254;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 8.0) == 0) {
                r = 255;
                g = 207;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 10.8) == 0) {
                r = 255;
                g = 141;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 13.9) == 0) {
                r = 255;
                g = 78;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 17.2) == 0) {
                r = 255;
                g = 0;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 20.8) == 0) {
                r = 193;
                g = 77;
                b = 0;
                a = 255;
            } else if (Double.compare(pvalue, 24.5) == 0) {
                r = 122;
                g = 4;
                b = 0;
            } else if (Double.compare(pvalue, 28.5) == 0) {
                r = 91;
                g = 28;
                b = 0;
                a = 255;
            }
//            else if (Double.compare(pvalue, 1.0) == 0) {
//                r = 85;
//                g = 78;
//                b = 177;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.1) == 0) {
//                r = 85;
//                g = 79;
//                b = 178;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.2) == 0) {
//                r = 84;
//                g = 80;
//                b = 178;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.3) == 0) {
//                r = 84;
//                g = 81;
//                b = 179;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.4) == 0) {
//                r = 83;
//                g = 82;
//                b = 180;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.5) == 0) {
//                r = 83;
//                g = 83;
//                b = 181;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.6) == 0) {
//                r = 82;
//                g = 83;
//                b = 181;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.7) == 0) {
//                r = 82;
//                g = 84;
//                b = 182;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.8) == 0) {
//                r = 81;
//                g = 85;
//                b = 183;
//                a = 255;
//            } else if (Double.compare(pvalue, 1.9) == 0) {
//                r = 81;
//                g = 86;
//                b = 183;
//                a = 255;
//            } else if (Double.compare(pvalue, 2.0) == 0) {
//                r = 80;
//                g = 87;
//                b = 184;
//                a = 255;
//            } else if (Double.compare(pvalue, 4.0) == 0) {
//                r = 67;
//                g = 105;
//                b = 196;
//                a = 255;
//            } else if (Double.compare(pvalue, 6.0) == 0) {
//                r = 64;
//                g = 160;
//                b = 180;
//                a = 255;
//            } else if (Double.compare(pvalue, 8.0) == 0) {
//                r = 78;
//                g = 193;
//                b = 103;
//                a = 255;
//            } else if (Double.compare(pvalue, 10.0) == 0) {
//                r = 104;
//                g = 209;
//                b = 79;
//                a = 255;
//            } else if (Double.compare(pvalue, 12.0) == 0) {
//                r = 157;
//                g = 221;
//                b = 68;
//                a = 255;
//            } else if (Double.compare(pvalue, 14.0) == 0) {
//                r = 182;
//                g = 226;
//                b = 63;
//                a = 255;
//            } else if (Double.compare(pvalue, 16.0) == 0) {
//                r = 199;
//                g = 230;
//                b = 59;
//                a = 255;
//            } else if (Double.compare(pvalue, 18.0) == 0) {
//                r = 220;
//                g = 234;
//                b = 55;
//                a = 255;
//            } else if (Double.compare(pvalue, 20.0) == 0) {
//                r = 235;
//                g = 217;
//                b = 53;
//                a = 255;
//            } else if (Double.compare(pvalue, 22.0) == 0) {
//                r = 234;
//                g = 193;
//                b = 55;
//                a = 255;
//            } else if (Double.compare(pvalue, 24.0) == 0) {
//                r = 234;
//                g = 164;
//                b = 62;
//                a = 255;
//            } else if (Double.compare(pvalue, 26.0) == 0) {
//                r = 233;
//                g = 135;
//                b = 69;
//                a = 255;
//            } else if (Double.compare(pvalue, 28.0) == 0) {
//                r = 227;
//                g = 103;
//                b = 87;
//                a = 255;
//            } else if (Double.compare(pvalue, 30.0) == 0) {
//                r = 217;
//                g = 66;
//                b = 114;
//                a = 255;
//            } else if (Double.compare(pvalue, 32.0) == 0) {
//                r = 184;
//                g = 47;
//                b = 97;
//                a = 255;
//            } else if (Double.compare(pvalue, 34.0) == 0) {
//                r = 167;
//                g = 33;
//                b = 87;
//                a = 255;
//            } else if (Double.compare(pvalue, 36.0) == 0) {
//                r = 147;
//                g = 23;
//                b = 78;
//                a = 255;
//            } else if (Double.compare(pvalue, 38.0) == 0) {
//                r = 108;
//                g = 21;
//                b = 42;
//                a = 255;
//            } else if (Double.compare(pvalue, 40.0) == 0) {
//                r = 89;
//                g = 17;
//                b = 35;
//                a = 255;
//            } else if (Double.compare(pvalue, 42.0) == 0) {
//                r = 43;
//                g = 0;
//                b = 1;
//                a = 255;
//            }
        } else if ("ECT".equals(eleType) || "ERH".equals(eleType)) {
            if (Double.compare(pvalue, 0.0) == 0) {
                r = 0;
                g = 85;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 10.0) == 0) {
                r = 0;
                g = 85;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 20.0) == 0) {
                r = 0;
                g = 162;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 30.0) == 0) {
                r = 0;
                g = 255;
                b = 173;
                a = 1;
            } else if (Double.compare(pvalue, 40.0) == 0) {
                r = 0;
                g = 255;
                b = 90;
                a = 1;
            } else if (Double.compare(pvalue, 50.0) == 0) {
                r = 0;
                g = 255;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 60.0) == 0) {
                r = 90;
                g = 255;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 70.0) == 0) {
                r = 181;
                g = 255;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 80.0) == 0) {
                r = 255;
                g = 255;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 90.0) == 0) {
                r = 255;
                g = 170;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 100.0) == 0) {
                r = 255;
                g = 85;
                b = 0;
                a = 1;
            }
        } else if ("VIS".equals(eleType)) {
            if (Double.compare(pvalue, 0.0) == 0) {
                r = 114;
                g = 44;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 0.1) == 0) {
                r = 159;
                g = 0;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 0.2) == 0) {
                r = 255;
                g = 2;
                b = 3;
                a = 1;
            } else if (Double.compare(pvalue, 0.5) == 0) {
                r = 255;
                g = 86;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 1.0) == 0) {
                r = 255;
                g = 211;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 2.0) == 0) {
                r = 239;
                g = 235;
                b = 53;
                a = 1;
            } else if (Double.compare(pvalue, 3.0) == 0) {
                r = 189;
                g = 251;
                b = 48;
                a = 1;
            } else if (Double.compare(pvalue, 5.0) == 0) {
                r = 120;
                g = 253;
                b = 55;
                a = 1;
            } else if (Double.compare(pvalue, 10.0) == 0) {
                r = 52;
                g = 251;
                b = 177;
                a = 1;
            } else if (Double.compare(pvalue, 15.0) == 0) {
                r = 106;
                g = 203;
                b = 231;
                a = 1;
            } else if (Double.compare(pvalue, 20.0) == 0) {
                r = 157;
                g = 233;
                b = 247;
                a = 1;
            } else if (Double.compare(pvalue, 30.0) == 0) {
                r = 255;
                g = 255;
                b = 255;
                a = 1;
            }
        } else if ("FOG".equals(eleType)) {
            if (Double.compare(pvalue, 0.0) == 0) {
                r = 0;
                g = 110;
                b = 100;
            } else if (Double.compare(pvalue, 50.0) == 0) {
                r = 0;
                g = 153;
                b = 153;
            } else if (Double.compare(pvalue, 200.0) == 0) {
                r = 0;
                g = 205;
                b = 205;
            } else if (Double.compare(pvalue, 500.0) == 0) {
                r = 100;
                g = 255;
                b = 255;
            } else if (Double.compare(pvalue, 1000.0) == 0) {
                r = 195;
                g = 255;
                b = 229;
            }
        } else if ("HZ".equals(eleType)) {
            if (Double.compare(pvalue, 0.0) == 0) {
                r = 50;
                g = 0;
                b = 24;
            } else if (Double.compare(pvalue, 1000.0) == 0) {
                r = 126;
                g = 0;
                b = 34;
            } else if (Double.compare(pvalue, 2000.0) == 0) {
                r = 169;
                g = 112;
                b = 0;
            } else if (Double.compare(pvalue, 3000.0) == 0) {
                r = 213;
                g = 203;
                b = 2;
            }
        } else if ("PPH".equals(eleType)) {
            if (Double.compare(pvalue, -1.0) == 0) {
                r = 238;
                g = 238;
                b = 238;
            } else if (Double.compare(pvalue, 1.0) == 0) {
                r = 166;
                g = 242;
                b = 143;
            } else if (Double.compare(pvalue, 2.0) == 0) {
                r = 255;
                g = 190;
                b = 239;
            } else if (Double.compare(pvalue, 3.0) == 0) {
                r = 190;
                g = 190;
                b = 190;
            } else if (Double.compare(pvalue, 4.0) == 0) {
                r = 155;
                g = 128;
                b = 0;
            }
        } else if ("WP3".equals(eleType)) {
            if (Double.compare(pvalue, 0.0) == 0) {
                r = 255;
                g = 255;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 1.0) == 0) {
                r = 208;
                g = 208;
                b = 208;
                a = 1;
            } else if (Double.compare(pvalue, 2.0) == 0) {
                r = 158;
                g = 158;
                b = 158;
                a = 1;
            } else if (Double.compare(pvalue, 4.0) == 0) {
                r = 255;
                g = 211;
                b = 187;
                a = 1;
            } else if (Double.compare(pvalue, 6.0) == 0) {
                r = 255;
                g = 187;
                b = 238;
                a = 1;
            } else if (Double.compare(pvalue, 7.0) == 0) {
                r = 170;
                g = 255;
                b = 136;
                a = 1;
            } else if (Double.compare(pvalue, 8.0) == 0) {
                r = 51;
                g = 187;
                b = 51;
                a = 1;
            } else if (Double.compare(pvalue, 9.0) == 0) {
                r = 102;
                g = 187;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 10.0) == 0) {
                r = 0;
                g = 0;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 11.0) == 0) {
                r = 255;
                g = 0;
                b = 255;
                a = 1;
            } else if (Double.compare(pvalue, 12.0) == 0) {
                r = 136;
                g = 0;
                b = 68;
                a = 1;
            } else if (Double.compare(pvalue, 14.0) == 0) {
                r = 204;
                g = 204;
                b = 204;
                a = 1;
            } else if (Double.compare(pvalue, 15.0) == 0) {
                r = 170;
                g = 170;
                b = 170;
                a = 1;
            } else if (Double.compare(pvalue, 16.0) == 0) {
                r = 119;
                g = 119;
                b = 119;
                a = 1;
            } else if (Double.compare(pvalue, 17.0) == 0) {
                r = 68;
                g = 68;
                b = 68;
                a = 1;
            } else if (Double.compare(pvalue, 18.0) == 0) {
                r = 170;
                g = 119;
                b = 17;
                a = 1;
            } else if (Double.compare(pvalue, 19.0) == 0) {
                r = 255;
                g = 136;
                b = 0;
                a = 1;
            } else if (Double.compare(pvalue, 20.0) == 0) {
                r = 238;
                g = 38;
                b = 138;
                a = 1;
            } else if (Double.compare(pvalue, 30.0) == 0) {
                r = 238;
                g = 138;
                b = 38;
                a = 1;
            } else if (Double.compare(pvalue, 31.0) == 0) {
                r = 161;
                g = 153;
                b = 135;
                a = 1;
            } else if (Double.compare(pvalue, 53.0) == 0) {
                r = 170;
                g = 153;
                b = 136;
                a = 1;
            }
        }
        return new Color(r, g, b, a);
    }

    public static int indexOfArray(double[] array, double ele) {
        for (int i = 0; i < array.length; i++) {
            double temp = array[i];
            if (temp == ele) {
                return i;
            }
        }
        return -1;
    }

}
