package com.hxgis.timer;

import com.hxgis.util.DateUtil;
import com.hxgis.util.impl.ReadLatLonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.print.DocFlavor;
import java.io.File;
import java.sql.SQLClientInfoException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Li Hong on 2018/5/26 0026.
 */
@Component
public class ReadLatLonServiceImpl implements ReadLatLonService {
    @Autowired
    GetData getData;
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void cc(float[][][] dataset, String fileName){


        List listaa = new ArrayList();
        String strDate = fileName.substring(0,12);
        Date date = null;
        String observetime = "";
        Date dateA=null;
        Date dateB=null;

        //String strDate="2005年04月22日";
        //注意：SimpleDateFormat构造函数的样式与strDate的样式必须相符
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm"); //加上时间
        //必须捕获异常
        try {
            date=simpleDateFormat.parse(strDate);
            //dateA = date;
            observetime = sDateFormat.format(date);
            //System.out.println(date);
        } catch(Exception px) {
            px.printStackTrace();
        }
        StringBuffer stringBuffer = new StringBuffer("insert into fore_pipe(pipeid,publishtime,foretime,er3)values");
        String timelevel= fileName.substring(8,10);      //几点
        int timeSize = dataset.length;          //时次长度
        String sql = "";                //sql语句
        double lat=0;
        double lon=0;
        String [] latlonStr=null;
        String []arr = null;
        short id = 0;
        int latlonsize ;
        List<Map<String,Object>> list = getData.readLatLon();

        for(int j=0;j<list.size();j++){                //得到所有的经纬度，,,
            String str = (String) list.get(j).get("POINTS");
            id = (short) list.get(j).get("ID");
            arr = str.split(";");
            latlonsize = arr.length;

            dateA = date;
            for(int b=0;b<timeSize;b++){                            //得到所有的时次

                dateB = DateUtil.calculateByHour(dateA,3);
                int sum = 0;
                double value = 0;

                int c=0;
                for(int i = 0;i<arr.length;i++){
                    latlonStr = arr[i].split(",");
                    lon =  Double.parseDouble(latlonStr[0]);
                    lat = Double.parseDouble(latlonStr[1]);
                    Map<String,Integer> maps = Util.test(lon,lat);//这里面包含每个经纬度的算出来的间隔
                    int lonx = maps.get("lon");
                    int laty = maps.get("lat");
                    try{
                        value+=dataset[b][laty][lonx];
                        sum++;
                    }catch (Exception e){
                        c++;
                        continue;
                    }
                     //System.out.println(dataset[b][lonx][lony]);
                }
                //System.out.println("我是行数"+j+"----我是每行多少数"+arr.length+"我是每行出错的数: "+c+"每行有效个数"+sum);
                double avg = value/sum;//求平均
                dateA = dateB;

                if(arr.length==c){

                }else {
                    sql = "("+id+",'"+observetime+"','"+sDateFormat.format(dateA)+"','"+avg+"'),";
                    listaa.add(sql);
                    stringBuffer.append(sql);
                }


            }

        }

        //System.out.println(stringBuffer.toString());
        System.out.println("预报数据"+listaa.size());
        //System.out.println("异常："+sum);

        jdbcTemplate.execute(stringBuffer.substring(0,stringBuffer.length()-1));

    }

}
