/**
  * Copyright 2021 json.cn 
  */
package com.iamverycute.weather.model;
import java.util.List;

/**
 * Auto-generated: 2021-10-03 22:54:14
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Data {

    private Yesterday yesterday;
    private String city;
    private List<Forecast> forecast;
    private String ganmao;
    private String wendu;
    public void setYesterday(Yesterday yesterday) {
         this.yesterday = yesterday;
     }
     public Yesterday getYesterday() {
         return yesterday;
     }

    public void setCity(String city) {
         this.city = city;
     }
     public String getCity() {
         return city;
     }

    public void setForecast(List<Forecast> forecast) {
         this.forecast = forecast;
     }
     public List<Forecast> getForecast() {
         return forecast;
     }

    public void setGanmao(String ganmao) {
         this.ganmao = ganmao;
     }
     public String getGanmao() {
         return ganmao;
     }

    public void setWendu(String wendu) {
         this.wendu = wendu;
     }
     public String getWendu() {
         return wendu;
     }

}