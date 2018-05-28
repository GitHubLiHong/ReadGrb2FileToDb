package com.hxgis.timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Li Hong on 2018/5/26 0026.
 */

//传递一个经纬度，获取这个经纬度对应的时次
    /*
    * x:那个时次
    * X表示经度隔开了多少个格子 ,也就是大的那个数，
    *Y表示纬度隔开了多少个格子，也就是小的那个数
    * */

@Component
public class GetData {
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private  static float [][][] eleValues;              //三维数组
   // private  static Float[][] two;
    public static void getRainfallToLonLat(int z,int x,int y,float [][][] eleValues){
        for (int i = z; i < eleValues.length; i++) {			// 遍历数组
            for (int j = x; j < eleValues[x].length; j++) {
                for (int k = y; k < eleValues[z][x].length; k++) {
                    //System.out.print(eleValues[z][x][y] + "\t");
                    return;
                }
                System.out.println();						// 输出一维数组后换行
            }
        }
    }


    //读取数据库读经纬度
    //读取经纬度，一行一行的读取，将每行经纬度进行分割，然后取每个经纬度对应的不同时次，然后求每个此时的对应的每行的平均值
    public List<Map<String,Object>> readLatLon(){
        String sql = "select * from GXQX.dbo.PipelineCoords";
        Map<String,Object> map =new HashMap<>();
        List<Map<String,Object>> list =  namedParameterJdbcTemplate.queryForList(sql,map);
        return list;
    }

}
