package ousoftoa.com.xmpp.model.bean;

import java.io.Serializable;

/**
 * Created by 韩莫熙 on 2017/4/24.
 */

public class LocationData implements Serializable {
    private String lat;
    private String lng;
    private String poi;
    private String imgUrl;


    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return "{" +
                "'lat':'" + lat + '\'' +
                ", 'lng':'" + lng + '\'' +
                ", 'poi':'" + poi + '\'' +
                ", 'imgUrl':'" + imgUrl + '\'' +
                '}';
    }
}

