package com.procore.prdiffs.model;

/* Model class used to classify diff split list*/
public class DiffDisplay {

    public enum DiffType {
        DIFF,
        HUNK_HEADER,
        LINE,
        SAME_LINE
    }

    private Object diffObj;
    private DiffType diffType;
    private String lineNum;

    public Object getDiffObj() {
        return diffObj;
    }

    public void setDiffObj(Object diffObj) {
        this.diffObj = diffObj;
    }

    public DiffType getDiffType() {
        return diffType;
    }

    public void setDiffType(DiffType diffType) {
        this.diffType = diffType;
    }

    public String getLineNum() {
        return lineNum;
    }

    public void setLineNum(String lineNum) {
        this.lineNum = lineNum;
    }
}
