package com.hxgis.model.geojson.geometry;



import com.hxgis.model.geojson.GeoType;

import java.util.List;

public class GeoGeometryMultiLineString extends GeoGeometry {

    public GeoGeometryMultiLineString() {
        this.type = GeoType.GEO_MULTILINESTRING;
    }

    private List<List<double[]>> coordinates;

    public List<List<double[]>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<double[]>> coordinates) {
        this.coordinates = coordinates;
    }

}
