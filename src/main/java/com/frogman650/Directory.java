package com.frogman650;

public class Directory {
    private String machineType;
    private String version;
    private String board;
    private String path;
    private String date;
    private String time;
    private String active;

    public Directory() {
        this.active = "";
        this.machineType = "";
        this.version = "";
        this.board = "";
        this.path = "";
        this.date = "";
        this.time = "";
    }

    public Directory(String active,String machineType, String version, String board, String path, String date, String time) {
        this.active = active;
        this.machineType = machineType;
        this.version = version;
        this.board = board;
        this.path = path;
        this.date = date;
        this.time = time;
    }

    public String getMachineType() {
        return machineType;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
