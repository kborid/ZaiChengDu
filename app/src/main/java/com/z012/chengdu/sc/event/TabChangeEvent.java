package com.z012.chengdu.sc.event;

public class TabChangeEvent {
    private String name;

    public TabChangeEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
