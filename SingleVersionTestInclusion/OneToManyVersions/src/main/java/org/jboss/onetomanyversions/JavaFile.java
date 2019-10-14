package org.jboss.onetomanyversions;

import java.util.ArrayList;
import java.util.List;

public class JavaFile {

    private String fileName;
    private int linenum;
    private int packagenum;
    private List<FileData> fileData;

    public List<FileData> getFileData() {
        return fileData;
    }

    public void setFileData(List<FileData> fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public JavaFile(String fileName, ArrayList<FileData> fileData, int linenum, int packagenum) {
        this.fileData = fileData;
        this.linenum = linenum;
        this.packagenum = packagenum;
        this.fileName = fileName;
    }

    public int getLinenum() {
        return linenum;
    }

    public void setLinenum(int linenum) {
        this.linenum = linenum;
    }

    public int getPackagenum() {
        return packagenum;
    }

    public void setPackagenum(int packagenum) {
        this.packagenum = packagenum;
    }
}
