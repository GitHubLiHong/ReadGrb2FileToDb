package com.hxgis.timer;

import com.hxgis.model.AccessGRBData;
import com.hxgis.util.impl.ReadLatLonService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ucar.ma2.Array;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Li Hong on 2018/5/27 0027.
 */

//读取GRB2文件入库
@Service
public class ReadGrbTwoToDB {
    @Autowired
    ReadLatLonService readLatLonService;

    private Map<String, String> unParsingMap;
    private boolean terminate = false;
    private Logger logger = Logger.getLogger(getClass());
    private boolean exit = false;

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean isExit() {
        return exit;
    }

    private float[][][] parseGrbSingleElementFile(String ymd, String hour, String path, String type,String fileName) throws Exception {
        AccessGRBData grbData = new AccessGRBData(path);
        grbData.ReadFile();
        List<GridDatatype> grids = grbData.getGrids();
        GeoGrid geoGrid = (GeoGrid) grids.get(0);
        boolean isTemperature = geoGrid.getUnitsString().equals("K");
        VariableDS var = geoGrid.getVariable();
//        int times = var.getShape(0);
        int[] shape = var.getShape();
        int[] origin = new int[shape.length];
        Array data2D = var.read(origin, shape).reduce();
        float[][][] eleValues = getDataArray(data2D);
        readLatLonService.cc(eleValues, fileName);
        return eleValues;
    }
    private static float[][][] getDataArray(Array data2D) {
        float[][][] dataF = null;
        try {
            dataF = (float[][][]) data2D.copyToNDJavaArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dataF;
    }


    public void parse(Map<String, String> changedFileMap,String FileName) throws Exception {
        logger.info("------------解析GRB文件线程开始------------");
        this.unParsingMap = changedFileMap;
        long st = System.currentTimeMillis();
        DateTime now = DateTime.now();
        String ymd = now.toString("yyyyMMdd");
        String hour = now.getHourOfDay() >= 17 ? "20" : "08";
//        String tmpPath = rootExtDir + "/tmp";
        //String tmpPath = "/opt/data/grb2Json/tmp";
        String tmpPath = "D:\\ER03";

        Iterator<Map.Entry<String, String>> it = unParsingMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> fileNamesEntry = it.next();
            if (!terminate) {
                String fileType = fileNamesEntry.getKey();
                String fileName = fileNamesEntry.getValue();
                File localFile = new File(tmpPath + "/" + fileName);
                //long startTime = System.currentTimeMillis();
                logger.info("开始解析" + fileName + "文件并裁剪");
                if ("EDA10".equals(fileType)) {
                    //parseGrbWindFile(ymd, hour, localFile.getAbsolutePath());
                } else {
                    parseGrbSingleElementFile(ymd, hour, localFile.getAbsolutePath(), fileType,FileName);
                }
                //long endTime = System.currentTimeMillis();
                //logger.info("解析" + localFile.getName() + "完成...耗时 ：" + (endTime - startTime) + "ms");
            }
            it.remove();
        }
        //long end = System.currentTimeMillis();
       // this.exit = true;
        //logger.info("------------解析GRB文件线程结束,耗时:" + (end - st) + " ms------------");
    }
    public synchronized Map<String, String> setTerminate(boolean terminate) {
        this.terminate = terminate;
        return unParsingMap;
    }

}
