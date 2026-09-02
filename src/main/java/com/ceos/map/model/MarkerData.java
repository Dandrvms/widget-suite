package com.ceos.map.model;

import com.gluonhq.maps.MapPoint;

/**
 *
 * @author Daniel
 * 
 * DTO for markers
 */
public class MarkerData {
        private final MapPoint point; 
        private final String name;
        private final String iconPath;
        private final String displayPath;
        public MarkerData(double lat, double lon, String name, String displayPath, String iconPath){
            this.point = new MapPoint(lat, lon);
            this.name = name;
            this.iconPath = iconPath;
            this.displayPath = displayPath;
        }

        public MarkerData(double lat, double lon){
            this.point = new MapPoint(lat, lon);
            this.name = null;
            this.iconPath = null;
            this.displayPath = null;
        }

        public MapPoint getPoint() { return point; }
        public String getName() { return name; }
        public String getIconPath() { return iconPath; }
        public String getDisplayPath() { return displayPath; }
}
