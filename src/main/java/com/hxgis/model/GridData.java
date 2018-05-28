package com.hxgis.model;


/**
 * 格点场
 *
 * @author BLQ
 */
public class GridData implements Cloneable {
    private String gridName;
    private double maxValue;
    private double minValue;
    private String unitStr;
    private double[] gridX;
    private double[] gridY;
    private double[][] grid;
    private double height;
    private long time;
    private String timeStr;

    public double[][] getGrid() {
        return grid;
    }

    public void setGrid(double[][] grid) {
        this.grid = grid;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }
    /*public List<GridPaintCell> getPaintCellsList() {
		return paintCellsList;
	}
	public void setPaintCellsList(List<GridPaintCell> paintCellsList) {
		this.paintCellsList = paintCellsList;
	}*/

    public double[] getGridY() {
        return gridY;
    }

    public void setGridY(double[] gridY) {
        this.gridY = gridY;
    }

    public double[] getGridX() {
        return gridX;
    }

    public void setGridX(double[] gridX) {
        this.gridX = gridX;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getUnitStr() {
        return unitStr;
    }

    public void setUnitStr(String unitStr) {
        this.unitStr = unitStr;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
