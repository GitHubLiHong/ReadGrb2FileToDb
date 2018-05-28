package com.hxgis.model.geojson.geometry;


import com.hxgis.model.geojson.GeoType;

public class GeoGeometryPoint extends GeoGeometry {

    public GeoGeometryPoint() {
        this.type = GeoType.GEO_POINT;
    }

    private double[] coordinates;

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

}
