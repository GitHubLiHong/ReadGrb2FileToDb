package com.hxgis.model;

import ucar.ma2.Array;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * GRIB2
 *
 * @author BLQ
 */

public class AnalysisData {

    public static List<GridData> Analysis(GridDatatype gridDataType)
            throws Exception {
        List<GridData> gridDatas = new ArrayList<GridData>();
        long tsize = 0, vsize = 0, ysize = 0, xsize = 0;
        GeoGrid grid = null;
        GridData gridData = null;
        GridCoordSystem coordinateSystem = null;
        Array tArray = null;
        Array vArray = null;
        Array yArray = null;
        Array xArray = null;
        Array valueArray = null;
        double scale = 1;//数据缩放比例 例如pa*0.01=hpa
        int t = 0, v = 0;
        double value = 0.0f, maxValue = -999999.0f, minValue = 999999.0f;
        long time = 0;
        float height = 0;
        double[][] dataArray = null;
        double[] gridX = null, gridY = null;
        grid = (GeoGrid) gridDataType;
        coordinateSystem = grid.getCoordinateSystem();
        if (coordinateSystem.getTimeAxis() != null) {
            tArray = coordinateSystem.getTimeAxis().read(); // time
            tsize = tArray.getSize();
        } else {
            tsize = 0;
        }
        if (coordinateSystem.getVerticalAxis() != null) {
            vArray = coordinateSystem.getVerticalAxis().read(); // height
            vsize = vArray.getSize();
        } else {
            vsize = 0;
        }
        if (coordinateSystem.getYHorizAxis() != null) {
            yArray = coordinateSystem.getYHorizAxis().read(); // lat
            ysize = yArray.getSize();
        } else {
            ysize = 0;
        }
        if (coordinateSystem.getXHorizAxis() != null) {
            xArray = coordinateSystem.getXHorizAxis().read(); // lon
            xsize = xArray.getSize();
        } else {
            xsize = 0;
        }
        t = 0;
        do {
            v = 0;
            do {
                gridData = new GridData();
                if (tArray != null) {
                    time = tArray.getLong(t);
                } else {
                    time = t;
                }
                if (vArray != null) {
                    height = vArray.getFloat(v);
                } else {
                    height = v;
                }
                if (grid.getUnitsString().equals("Pa")) {
                    gridData.setUnitStr("HPa");   //气象习惯性表达 百帕
                    scale = 0.01f;
                } else if(grid.getUnitsString().equals("1/s")){
                    gridData.setUnitStr("10-5s-1");   //气象习惯性表达
                    scale = 100000;
                }else if(grid.getUnitsString().equals("gpm")){
                    gridData.setUnitStr("10gpm");   //气象习惯性表达
                    scale = 0.1;
                }else if(grid.getUnitsString().equals("kg.m-2")){
                    gridData.setUnitStr("mm/"+time+"h");   //气象习惯性表达

                }else {
                    gridData.setUnitStr(grid.getUnitsString());
                }
                gridData.setGridName(grid.getFullName());
                gridData.setHeight(height);
                gridData.setTime(time);
                maxValue = -999999999.0f;
                minValue = 9999999999.0f;
                valueArray = grid.readYXData(t, v);
                dataArray = new double[(int) ysize][(int) xsize];
                for (int i = 0; i < ysize; i++) {
                    for (int j = 0; j < xsize; j++) {
                        //valueArray = grid.readDataSlice(t, v, i,j);
                        value = valueArray.getDouble(i * (int) xsize + j) * scale;
                        if (!Double.isNaN(value)) {
                            if (value > maxValue) {
                                maxValue = value;
                            }
                            if (value < minValue) {
                                minValue = value;
                            }
                        } else {
                            value = 0.0f;
                        }
                        if (yArray.getDouble(0) < yArray.getDouble(1)) {
                            dataArray[i][j] = value;
                        }else{
                            dataArray[(int)ysize-i-1][j] = value;
                        }
                    }
                }
                gridX = new double[(int) xsize];
                for (int i = 0; i < xsize; i++) {
                    gridX[i] = xArray.getDouble(i);
                }
                gridY = new double[(int) ysize];

                if (yArray.getDouble(0) < yArray.getDouble(1)) {
                    for (int i = 0; i < ysize; i++) {
                        gridY[i] = yArray.getDouble(i);
                    }
                } else {
                    for (int i = 0; i < ysize; i++) {
                        gridY[i] = yArray.getDouble((int) (ysize - i - 1));
                    }
                }
                gridData.setMaxValue(maxValue);
                gridData.setMinValue(minValue);
                gridData.setTimeStr(grid.getTimes().get(0).getName());
                gridData.setGridX(gridX);
                gridData.setGridY(gridY);
                gridData.setGrid(dataArray);
                gridDatas.add(gridData);
                v++;
            } while (v < vsize);
            t++;
        } while (t < tsize);
        return gridDatas;
    }

/**
     * 按照范围裁剪格点
     * @param grid
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @return
     */

    public static GridData ClipGridData(GridData grid, double minX, double maxX, double minY, double maxY) {
        GridData cgrid = new GridData();
        cgrid.setTime(grid.getTime());
        cgrid.setTimeStr(grid.getTimeStr());
        cgrid.setGridName(grid.getGridName());
        cgrid.setUnitStr(grid.getUnitStr());

        int startX = 0, endX = 0, startY = 0, endY = 0;
        if (minX <= grid.getGridX()[0]) {
            startX = 0;
        } else {
            for (int i = 1; i < grid.getGridX().length; i++) {
                if (grid.getGridX()[i] > minX) {
                    startX = i - 1;
                    break;
                }
            }

        }

        if (maxX >= grid.getGridX()[grid.getGridX().length - 1]) {
            endX = grid.getGridX().length - 1;
        } else {
            for (int i = grid.getGridX().length - 1; i >= 0; i--) {
                if (grid.getGridX()[i] < maxX) {
                    endX = i + 1;
                    break;
                }
            }
        }

        if (minY <= grid.getGridY()[0]) {
            startY = 0;
        } else {
            for (int i = 1; i < grid.getGridY().length; i++) {
                if (grid.getGridY()[i] > minY) {
                    startY = i - 1;
                    break;
                }
            }
        }

        if (maxY >= grid.getGridY()[grid.getGridY().length - 1]) {
            endY = grid.getGridY().length - 1;
        } else {
            for (int i = grid.getGridY().length - 1; i >= 0; i--) {
                if (grid.getGridY()[i] < maxY) {
                    endY = i + 1;
                    break;
                }
            }
        }


        int xNum = endX - startX + 1;
        int yNum = endY - startY + 1;

        double[][] cData = new double[yNum][xNum];
        double[] cX = new double[xNum];
        double[] cY = new double[yNum];

        for (int i = 0; i < xNum; i++) {
            cX[i] = grid.getGridX()[startX + i];
        }

        for (int i = 0; i < yNum; i++) {
            cY[i] = grid.getGridY()[startY + i];
        }


        double maxValue = -999999999.0;
        double minValue = 9999999999.0;
        double value=0;
        for (int i = 0; i < xNum; i++) {
            for (int j = 0; j < yNum; j++) {
                value=grid.getGrid()[startY+j][startX+i];
                if (value > maxValue) {
                    maxValue = value;
                }
                if (value < minValue) {
                    minValue = value;
                }
                cData[j][i] = value;
            }
        }

        cgrid.setMaxValue(maxValue);
        cgrid.setMinValue(minValue);
        cgrid.setGridX(cX);
        cgrid.setGridY(cY);
        cgrid.setGrid(cData);

        return cgrid;
    }
}
