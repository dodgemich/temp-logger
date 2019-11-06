package com.dodge.furnace;

public class FurnaceReading {
    
    private String state;
    private Long humidity;
    private Long target;
    private Long ambient;
    private Long outside;
    
    public Long getOutside() {
        return outside;
    }
    public void setOutside(Long outside) {
        this.outside = outside;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public Long getHumidity() {
        return humidity;
    }
    public void setHumidity(Long humidity) {
        this.humidity = humidity;
    }
    public Long getTarget() {
        return target;
    }
    public void setTarget(Long target) {
        this.target = target;
    }
    public Long getAmbient() {
        return ambient;
    }
    public void setAmbient(Long ambient) {
        this.ambient = ambient;
    }

}
