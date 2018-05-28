package com.hxgis.model;

import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

import java.io.File;
import java.util.List;

/**
 * GRIB2格式读取类
 *
 * @author BLQ
 */

public class AccessGRBData {
    private String filePath;
    private List<GridDatatype> grids;

    public GridDataset getDataset() {
        return dataset;
    }

    public void setDataset(GridDataset dataset) {
        this.dataset = dataset;
    }

    private GridDataset dataset;

    public List<GridDatatype> getGrids() {
        return grids;
    }

    public void setGrids(List<GridDatatype> grids) {
        this.grids = grids;
    }

    public AccessGRBData(String filePath) {
        this.filePath = filePath;
    }


    /**
     * 读取格点场
     *
     * @throws Exception
     */

    public void ReadFile() throws Exception {
        try {
            dataset = GridDataset.open(filePath);
            grids = dataset.getGrids();

            for(GridDatatype gdt:grids){
                VariableDS  a = gdt.getVariable();
            }

        } catch (Exception e) {
            throw new Exception("Generate Pictures Failed" + e.getMessage());
        }
    }

    /**
     * 关闭数据读取流
     */

    public void Dispose() {
        if (dataset != null) {
            try {
                dataset.close();
                File gbx9file = new File(filePath + ".gbx9");
                if (gbx9file.exists()) {
                    gbx9file.delete();
                }
                File ncxfile = new File(filePath + ".ncx3");
                if (ncxfile.exists()) {
                    ncxfile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
