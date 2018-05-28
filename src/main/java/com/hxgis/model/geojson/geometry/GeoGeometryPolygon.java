package com.hxgis.model.geojson.geometry;



import com.hxgis.model.geojson.GeoType;

import java.util.List;

public class GeoGeometryPolygon extends GeoGeometry {

    public GeoGeometryPolygon() {
        this.type = GeoType.GEO_POLYGON;
    }

    private List<List<double[]>> coordinates;

    public List<List<double[]>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<double[]>> coordinates) {
        this.coordinates = coordinates;
    }

}
