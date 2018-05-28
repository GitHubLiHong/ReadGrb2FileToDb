package com.hxgis.timer;

import com.hxgis.common.Constant;
import com.sleepycat.je.tree.IN;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Li Hong on 2018/5/26 0026.
 */
public class Util {
    public static  Map<String,Integer> test (double lon, double lat){
        Map<String,Integer> map = new HashMap<>();
        //double xavg = (Constant.GRB2_LNGMAX_HB-Constant.GRB2_LNGMIN_HB)/2;
       // double yavg = (Constant.GRB2_LATMIN_HB-Constant.GRB2_LATMAX_HB)/2;


        double x = (lon-Constant.GRB2_LNGMIN_HB)/Constant.GRB2_LNGGAP;
        double y = (lat-Constant.GRB2_LATMIN_HB)/Constant.GRB2_LATGAP;
        int i = Integer.parseInt(String.valueOf(Math.round(x)));
        int z = Integer.parseInt(String.valueOf(Math.round(y)));
        map.put("lon",i);
        map.put("lat",z);
        return map;
    }
}
