package com.hxgis.model.geojson.geometry;



import com.hxgis.model.geojson.GeoType;

import java.util.List;

public class GeoGeometryLineString extends GeoGeometry {

    public GeoGeometryLineString() {
        this.type = GeoType.GEO_LINESTRING;
    }

    private List<double[]> coordinates;

    public List<double[]> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<double[]> coordinates) {
        this.coordinates = coordinates;
    }

}
