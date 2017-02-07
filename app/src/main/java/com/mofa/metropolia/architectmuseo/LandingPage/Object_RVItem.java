package com.mofa.metropolia.architectmuseo.LandingPage;


public class Object_RVItem {
    private String img_base64;
    private String cataName;

    public Object_RVItem(String cata ,String img_base64) {
        this.img_base64 = img_base64;
        this.cataName = cata;
    }

    public String getImg_base64() {
        return img_base64;
    }

    public String getCataName() {
        return cataName;
    }

    public void setImg_base64(String img_base64) {
        this.img_base64 = img_base64;
    }

    public void setCataName(String cataName) {
        this.cataName = cataName;
    }
}
