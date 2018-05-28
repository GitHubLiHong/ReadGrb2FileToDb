package com.hxgis.timer;


import com.hxgis.util.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Li Hong on 2018/5/27 0027.
 */
//ER03文件下载（逐3小时降水量）

//@Component
@RestController
@RequestMapping("dd")
public class FileDownloadTimer {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ParseGrbFileService parseGrbFileService;
    @Autowired
    ReadGrbTwoToDB readGrbTwoToDB;

    @Scheduled(cron="0 0/5 * * * ?")
    public void Json(){
        try {
            String url = "http://int.scqx.net/";
            String param = "id=1194&key=6b63dddaf182ee8a";
            String rtn = HttpRequest.sendGet(url, param);

           JSONObject jo = new JSONObject(rtn);
           JSONArray cdata = jo.getJSONArray("cdata");

            String sid;
            String st;
            String t;
            String gst80;
            String pr1;
            String pr3;
            String pr6;
            String pr12;
            String pr24;
            String ws;
            String wd;

            List<String> sql_list = new ArrayList();
            String sql_sk = "";

            for (int i=0; i<cdata.length(); i++)
            {
                JSONObject obj = cdata.getJSONObject(i);

                sid = (obj.getString("sid"));
                st = (obj.getString("st"));

                String time ="";
                for(int y = 0;y<25;y++){
                    if(!st.substring(y,y+1).equals(":")){
                        if(!st.substring(y,y+1).equals("/")){
                            time = time + st.substring(y,y+1);
                        }else {
                            time = time +"-";
                        }
                    }else {
                        time = time + ":00:00";
                        break;
                    }
                }
                List<String> listWfNwp = jdbcTemplate.queryForList("select distinct(sid) FROM sk_pipe WHERE sid ='"+sid+"' and st = '"+time+"'",String.class);
                if(listWfNwp.size() >0){
                    continue;
                }
                t = (obj.getString("t"));
                gst80 = (obj.getString("gst80"));
                pr1 = (obj.getString("pr1"));
                pr3 = (obj.getString("pr3"));
                pr6 = (obj.getString("pr6"));
                pr12 = (obj.getString("pr12"));
                pr24 = (obj.getString("pr24"));
                ws = (obj.getString("ws"));
                wd = (obj.getString("wd"));

                if(pr1.length()>0&&pr1.substring(0,1).equals(".")){
                    pr1 = "0"+pr1;
                }

                if(pr3.length()>0&&pr3.substring(0,1).equals(".")){
                    pr3 = "0"+pr3;
                }

                if(pr6.length()>0&&pr6.substring(0,1).equals(".")){
                    pr6 = "0"+pr6;
                }

                if(pr12.length()>0&&pr12.substring(0,1).equals(".")){
                    pr12 = "0"+pr12;
                }

                if(pr24.length()>0&&pr24.substring(0,1).equals(".")){
                    pr24 = "0"+pr24;
                }
                if(gst80.length()>0){
                    sql_sk = "insert into sk_pipe (sid,st,t,gst80,pr1,pr3,pr6,pr12,pr24,ws,wd) " +
                            "VALUES ('"+sid+"','"+time+"','"+t+"','"+gst80+"','"+pr1+"','"+pr3+"','"+pr6+"','"+pr12+"','"+pr24+"','"+ws+"','"+wd+"')";
                    sql_list.add(sql_sk);
                }else {
                    sql_sk = "insert into sk_pipe (sid,st,t,pr1,pr3,pr6,pr12,pr24,ws,wd) " +
                            "VALUES ('"+sid+"','"+time+"','"+t+"','"+pr1+"','"+pr3+"','"+pr6+"','"+pr12+"','"+pr24+"','"+ws+"','"+wd+"')";
                    sql_list.add(sql_sk);
                }

            }
            if(sql_list.size()>0){
                System.out.println("----------本次共有"+sql_list.size()+"条数据待更新");
                int r = 0;

                for(String sql : sql_list){
                    jdbcTemplate.execute(sql);
                    r++;
                }

                System.out.println("更新成功"+r+"条");
            }else {
                System.out.println("暂无更新");
            }




        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Scheduled(cron="0 0 0/1 * * ? ")
    //@RequestMapping("/aa")
    public void start() {
        Map<String,String> map = new HashMap<>();
        String url = "http://int.scqx.net/";
        String param = "id=1195&key=1a5c5400173bac68 ";
        String localpath= "D:/ER03/";
        String sType="ER03";
        String fileName = "";
        String rtn = HttpRequest.sendGet(url, param);

        JSONObject jo = new JSONObject(rtn);
        JSONArray cdata = jo.getJSONArray("cdata");

        for (int i = 0; i < cdata.length(); i++) {
            JSONObject obj = cdata.getJSONObject(i);
            String str = obj.getString("imgUrlWan");

            if(obj.getString("sType").equals(sType)){
                fileName=   obj.getString("ft");
                try {
                    File saveDir = new File(""+localpath+fileName+".GRB2");
                    if( judeFileExists(saveDir)==false){
                        downloadFile(str,localpath+fileName+".GRB2");
                        map.put("ER03", ""+fileName+".GRB2");
                        readGrbTwoToDB.parse(map,fileName);
                    }
                }catch (Exception e){

                }
            }

        }
    }

    /*
    * 通过访问url 下载文件
    * fileUrl:请求路径
    * fileLocal：本地下载地址
    * */
    public static  void downloadFile(String fileUrl,String fileLocal) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
        urlCon.setConnectTimeout(6000);
        urlCon.setReadTimeout(6000);
        int code = urlCon.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new Exception("文件读取失败");
        }

        //读文件流
        DataInputStream in = new DataInputStream(urlCon.getInputStream());
        DataOutputStream out = new DataOutputStream(new FileOutputStream(fileLocal));
        byte[] buffer = new byte[2048];
        int count = 0;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.close();
        in.close();
    }

    /*判断文件是否存在*/
    public static boolean judeFileExists(File file) {

        if (file.exists()) {
            System.out.println("file exists");
            return true;
        } else {
            System.out.println("file not exists, create it ...");
            return false;
                         /*try {
                                 file.createNewFile();
                             } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                            }*/
        }

    }

}
