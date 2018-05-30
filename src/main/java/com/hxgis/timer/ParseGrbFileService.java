package com.hxgis.timer;

import com.hxgis.common.Constant;
import com.hxgis.model.MoapEntity.*;
import com.hxgis.model.geojson.GeoFeatureCollection;
import com.hxgis.util.MoapUtil;
import com.hxgis.util.impl.ReadLatLonService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ucar.ma2.Array;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by admin on 2018/1/18.
 */
@Service
public class ParseGrbFileService {
    @Autowired
    ReadLatLonService readLatLonService;

    private Logger logger = Logger.getLogger(getClass());

    //@Value("${grb.extern-dir}")
    private String rootExtDir = "d:/grb2";

    private Map<String, String> unParsingMap;

    private boolean exit = false;

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean isExit() {
        return exit;
    }

    private static String bounds = "";

    private boolean terminate = false;

    public synchronized Map<String, String> setTerminate(boolean terminate) {
        this.terminate = terminate;
        return unParsingMap;
    }

    public boolean getTerminate() {
        return this.terminate;
    }

    public void parse(Map<String, String> changedFileMap) throws Exception {
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
                long startTime = System.currentTimeMillis();
                logger.info("开始解析" + fileName + "文件并裁剪");
                if ("EDA10".equals(fileType)) {
                    parseGrbWindFile(ymd, hour, localFile.getAbsolutePath());
                } else {
                     parseGrbSingleElementFile(ymd, hour, localFile.getAbsolutePath(), fileType);
                }
                long endTime = System.currentTimeMillis();
                logger.info("解析" + localFile.getName() + "完成...耗时 ：" + (endTime - startTime) + "ms");
            }
            it.remove();
        }
        long end = System.currentTimeMillis();
        this.exit = true;
        logger.info("------------解析GRB文件线程结束,耗时:" + (end - st) + " ms------------");
    }


    //public static void main(String[] args) throws Exception {
       // Map<String, String> map = new HashMap<>();
        //map.put("EDA10", "Z_NWGD_C_BCWH_20180205100000_P_RFFC_SPCC-EDA10_201802050800_24003.GRB2");
       // map.put("EDA10", "3AD33197CF46B52B.GRB2");

       // ParseGrbFileService service = new ParseGrbFileService();
        //service.parse(map);
       // AccessGRBData accessGRBData = new AccessGRBData("d:/grb2/3AD33197CF46B52B.GRB2");
        //accessGRBData.ReadFile();
    //}


    public static void main() throws Exception{

        Map<String, String> map = new HashMap<>();
        //map.put("EDA10", "Z_NWGD_C_BCWH_20180205100000_P_RFFC_SPCC-EDA10_201802050800_24003.GRB2");
        //map.put("ER03", "Z_NWGD_C_BCWH_20180214101311_P_RFFC_SPCC-ER03_201802140800_24003 (1).GRB2");
        map.put("ER03", "201805262000.GRB2");


        ParseGrbFileService service = new ParseGrbFileService();
        service.parse(map);

    }

    private float[][][] parseGrbSingleElementFile(String ymd, String hour, String path, String type) throws Exception {
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

        readLatLonService.cc(eleValues,"201805272000");
        return eleValues;

        //readLatLonService.main(eleValues,"44444");


        //GetData.getRainfallToLonLat(0,0,0,eleValues);

        //System.out.println(eleValues[0][1][2]);
       /* String commonPath = rootExtDir + "/" + ymd + hour + "/" + type;
        String gridJsonFilePath = commonPath + "/gridJson";
        String featureJsonFilePath = commonPath + "/featureJson";
        String geoJsonFilePath = commonPath + "/geoJson";
        File gridJsonFile = new File(gridJsonFilePath);
        if (!gridJsonFile.exists()) {
            gridJsonFile.mkdirs();
        }
        File featureJsonFile = new File(featureJsonFilePath);
        if (!featureJsonFile.exists()) {
            featureJsonFile.mkdirs();
        }
        File geoJsonFile = new File(geoJsonFilePath);
        if (!geoJsonFile.exists()) {
            geoJsonFile.mkdirs();
        }
        for (int i = 0; i < eleValues.length; i++) {
//        for (int i = 0; i < times; i++) {
            if (!this.terminate) {
//                Array data2D = null;
//                try {
//                    data2D = geoGrid.readYXData(i, 0);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (data2D != null) {
//                    float[][] eleValues = (float[][]) data2D.copyToNDJavaArray();
                String curTimeCnt;
                if (eleValues.length == 10) {
                    curTimeCnt = StringUtils.leftPad(String.valueOf((i + 1) * 24), 3, "0");
                } else {
                    curTimeCnt = StringUtils.leftPad(String.valueOf((i + 1) * 3), 3, "0");
                }
                String gridJsonFileName = gridJsonFilePath + "/" + curTimeCnt + ".json";
                String featureJsonFileName = featureJsonFilePath + "/" + curTimeCnt + ".json";
                String geoJsonFileName = geoJsonFilePath + "/" + curTimeCnt + ".json";
                saveGrbSingleElementData2GridJsonFile(eleValues[i], Constant.GRB2_XSIZE, Constant.GRB2_YSIZE, gridJsonFileName, isTemperature);
                saveGrbSingleElementData2FeatureJsonFile(eleValues[i], type, featureJsonFileName, geoJsonFileName, isTemperature);
                logger.debug("成功生成 " + ymd + "日" + hour + "时 " + type + " " + curTimeCnt + "时次的JSON文件");
//                }
            }
        }
        grbData.Dispose();*/
    }

    private void parseGrbWindFile(String ymd, String hour, String path) throws Exception {
        AccessGRBData grbData = new AccessGRBData(path);
        grbData.ReadFile();
        List<GridDatatype> grids = grbData.getGrids();
        float[][][] uValues = null;
        float[][][] vValues = null;
        for (GridDatatype grid : grids) {
            String desc = grid.getDescription();
            VariableDS var = grid.getVariable();
            int[] shape = var.getShape();
            int[] origin = new int[shape.length];
            if (desc.contains("u-component")) {
                uValues = getDataArray(var.read(origin, shape).reduce());
            } else {
                vValues = getDataArray(var.read(origin, shape).reduce());
            }
        }
        grbData.Dispose();
        String commonPath = rootExtDir + "/" + ymd + hour + "/EDA10";
        String gridJsonFilePath = commonPath + "/gridJson";
        String featureJsonFilePath = commonPath + "/featureJson";
        String geoJsonFilePath = commonPath + "/geoJson";
        File gridJsonFile = new File(gridJsonFilePath);
        if (!gridJsonFile.exists()) {
            gridJsonFile.mkdirs();
        }
        File featureJsonFile = new File(featureJsonFilePath);
        if (!featureJsonFile.exists()) {
            featureJsonFile.mkdirs();
        }
        File geoJsonFile = new File(geoJsonFilePath);
        if (!geoJsonFile.exists()) {
            geoJsonFile.mkdirs();
        }
        for (int i = 0; i < uValues.length; i++) {
            if (!this.terminate) {
                String curTimeCnt;
                if (uValues.length == 10) {
                    curTimeCnt = StringUtils.leftPad(String.valueOf((i + 1) * 24), 3, "0");
                } else {
                    curTimeCnt = StringUtils.leftPad(String.valueOf((i + 1) * 3), 3, "0");
                }
                String gridJsonFileName = gridJsonFilePath + "/" + curTimeCnt + ".json";
                String featureJsonFileName = featureJsonFilePath + "/" + curTimeCnt + ".json";
                String geoJsonFileName = geoJsonFilePath + "/" + curTimeCnt + ".json";
                float[][] windSArr = saveGrbWindData2GridJsonFile(uValues[i], vValues[i], gridJsonFileName);
                saveGrbSingleElementData2FeatureJsonFile(windSArr, "EDA10", featureJsonFileName, geoJsonFileName, false);
                logger.debug("成功生成 " + ymd + "日" + hour + "时 EDA10" + " " + curTimeCnt + "时次的JSON文件");
            }
        }
    }

    private float[][] saveGrbWindData2GridJsonFile(float[][] uValueArray, float[][] vValueArray, String gridJsonFileName) {
        GridJson gridJson = createGridJson();
        float[][] gridData = new float[4][Constant.GRB2_YSIZE * Constant.GRB2_XSIZE];
        float[][] windSArr = new float[Constant.GRB2_YSIZE][Constant.GRB2_XSIZE];
        for (int i = 0; i < Constant.GRB2_YSIZE; i++) {
            for (int j = 0; j < Constant.GRB2_XSIZE; j++) {
                float uValue = uValueArray[i][j];
                float vValue = vValueArray[i][j];
                int index = i * Constant.GRB2_XSIZE + j;
                //四个要素依次分别为 风速 风向 u v;
                float windS = new BigDecimal(Math.sqrt(uValue * uValue + vValue * vValue)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                windSArr[i][j] = windS;
                gridData[0][index] = windS;
                gridData[1][index] = new BigDecimal(180 + Math.atan2(uValue, vValue) * 180 / Math.PI).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                gridData[2][index] = uValue;
                gridData[3][index] = vValue;
            }
        }
        gridJson.getData().getGrids().get(0).getProperties().setGridDatas(gridData);
        try (FileOutputStream out = new FileOutputStream(new File(gridJsonFileName))) {
            String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(gridJson);
            IOUtils.write(jsonString, out, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return windSArr;
    }

    private void saveGrbSingleElementData2FeatureJsonFile(float[][] valueArray, String type, String featureJsonFileName, String geoJsonFileName, boolean isTemperature) {
        double[] xs = new double[Constant.GRB2_XSIZE];
        double[] ys = new double[Constant.GRB2_YSIZE];
        for (int i = 0; i < Constant.GRB2_XSIZE; i++) {
            xs[i] = new BigDecimal(Constant.GRB2_LNGMIN_HB + i * Constant.GRB2_LNGGAP).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        for (int j = 0; j < Constant.GRB2_YSIZE; j++) {
            ys[j] = new BigDecimal(Constant.GRB2_LATMIN_HB + j * Constant.GRB2_LATGAP).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        double[][] value = new double[Constant.GRB2_YSIZE][Constant.GRB2_XSIZE];
        for (int k = 0; k < Constant.GRB2_YSIZE; k++) {
            int l = 0;
            for (; l < Constant.GRB2_XSIZE; l++) {
                double tempValue = valueArray[k][l];
                if (isTemperature) {
                    tempValue = new BigDecimal(tempValue - 273.15).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();//K转为℃
                } else {
                    if (tempValue > 99999) {
                        tempValue = 0;
                    }
                }
                value[k][l] = new BigDecimal(tempValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
        }

        Map<String, Object> map = MoapUtil.dataToPolygonJson(xs, ys, value, type, bounds);
        if (map != null && map.get("featureJson") != null) {
            FeatureJson featureJson = (FeatureJson) map.get("featureJson");

            try (FileOutputStream out = new FileOutputStream(new File(featureJsonFileName))) {
                String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(featureJson);
                IOUtils.write(jsonString, out, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (map != null && map.get("geoJson") != null) {
            GeoFeatureCollection geoFeatureCollection = (GeoFeatureCollection) map.get("geoJson");
            try (FileOutputStream out = new FileOutputStream(new File(geoJsonFileName))) {
                String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(geoFeatureCollection);
                IOUtils.write(jsonString, out, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void saveGrbSingleElementData2GridJsonFile(float[][] valueArray, int xSize, int ySize, String jsonFileName, boolean isTemperature) {
        GridJson gridJson = createGridJson();
        float[][] gridData = new float[1][ySize * xSize];
        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                float value = valueArray[i][j];
                if (isTemperature) {
                    value = new BigDecimal(value - 273.15).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();//K转为℃
                } else {
                    if (value > 99999) {//负值按照0处理
                        value = 0;
                    }
                }
                gridData[0][i * xSize + j] = value;
            }
        }
        gridJson.getData().getGrids().get(0).getProperties().setGridDatas(gridData);
        try (FileOutputStream out = new FileOutputStream(new File(jsonFileName))) {
            String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(gridJson);
            IOUtils.write(jsonString, out, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private GridJson createGridJson() {
        GridJson gridJson = new GridJson();
        gridJson.setStatus("1");
        GridCollection data = new GridCollection();
        gridJson.setData(data);
        data.setType("GridCollection");
        data.setProperties(new Properties());
        data.getProperties().put("date", 1);
        data.getProperties().put("type", "grb2");
        List<Grid> gridList = new ArrayList<>();
        Grid grid = new Grid();
        gridList.add(grid);
        data.setGrids(gridList);
        grid.setProperties(new GridProperties());
        grid.getProperties().setAnalyseStep(0);
        grid.getProperties().setColumn(Constant.GRB2_XSIZE);
        grid.getProperties().setRow(Constant.GRB2_YSIZE);
        grid.getProperties().setStartLat(Constant.GRB2_LATMIN_HB);
        grid.getProperties().setEndLat(Constant.GRB2_LATMAX_HB);
        grid.getProperties().setStartLon(Constant.GRB2_LNGMIN_HB);
        grid.getProperties().setEndLon(Constant.GRB2_LNGMAX_HB);
        grid.getProperties().setLatGap(Constant.GRB2_LATGAP);
        grid.getProperties().setLonGap(Constant.GRB2_LNGGAP);
        return gridJson;
    }


    private void test(String ymd, String hour, String path, String type) throws Exception {
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
        String commonPath = rootExtDir + "/" + ymd + hour + "/" + type;
        String gridJsonFilePath = commonPath + "/gridJson";
        String featureJsonFilePath = commonPath + "/featureJson";
        String geoJsonFilePath = commonPath + "/geoJson";
        File gridJsonFile = new File(gridJsonFilePath);
        if (!gridJsonFile.exists()) {
            gridJsonFile.mkdirs();
        }
        File featureJsonFile = new File(featureJsonFilePath);
        if (!featureJsonFile.exists()) {
            featureJsonFile.mkdirs();
        }
        File geoJsonFile = new File(geoJsonFilePath);
        if (!geoJsonFile.exists()) {
            geoJsonFile.mkdirs();
        }
        for (int i = 0; i < eleValues.length; i++) {
//        for (int i = 0; i < times; i++) {
            if (!this.terminate) {
//                Array data2D = null;
//                try {
//                    data2D = geoGrid.readYXData(i, 0);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (data2D != null) {
//                    float[][] eleValues = (float[][]) data2D.copyToNDJavaArray();
                String curTimeCnt;
                if (eleValues.length == 10) {
                    curTimeCnt = StringUtils.leftPad(String.valueOf((i + 1) * 24), 3, "0");
                } else {
                    curTimeCnt = StringUtils.leftPad(String.valueOf((i + 1) * 3), 3, "0");
                }
                String gridJsonFileName = gridJsonFilePath + "/" + curTimeCnt + ".json";
                String featureJsonFileName = featureJsonFilePath + "/" + curTimeCnt + ".json";
                String geoJsonFileName = geoJsonFilePath + "/" + curTimeCnt + ".json";
                saveGrbSingleElementData2GridJsonFile(eleValues[i], Constant.GRB2_XSIZE, Constant.GRB2_YSIZE, gridJsonFileName, isTemperature);
                saveGrbSingleElementData2FeatureJsonFile(eleValues[i], type, featureJsonFileName, geoJsonFileName, isTemperature);
                logger.debug("成功生成 " + ymd + "日" + hour + "时 " + type + " " + curTimeCnt + "时次的JSON文件");
//                }
            }
        }
        grbData.Dispose();
    }

}
