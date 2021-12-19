package me.friwi.jcefmavenbot.issues;

import java.util.Objects;

public class TestResult {
    private EnumPlatform platform;
    private EnumTestResult testResult;
    private String href;

    protected TestResult(EnumPlatform platform, EnumTestResult testResult, String href) {
        this.platform = platform;
        this.testResult = testResult;
        this.href = href;
    }

    public EnumPlatform getPlatform() {
        return platform;
    }

    public EnumTestResult getTestResult() {
        return testResult;
    }

    public String getHref() {
        return href;
    }

    public void setTestResult(EnumTestResult testResult) {
        this.testResult = testResult;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestResult that = (TestResult) o;
        return getPlatform() == that.getPlatform() && getTestResult() == that.getTestResult() && Objects.equals(getHref(), that.getHref());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlatform(), getTestResult(), getHref());
    }

    @Override
    public String toString() {
        EnumTestResult tr = this.getTestResult();
        if(tr==null)tr=EnumTestResult.UNTESTED;
        String link = href;
        if(link==null)link="#";
        return "[!["+tr.getValue()+"](https://img.shields.io/badge/"+platform.getOS().getValue()+"--"+platform.getArch().getValue()+"-"+tr.getValue()+"-"+tr.getBadgeColor()+")]("+link+")";
    }

    public static TestResult fromString(EnumPlatform platform, String str){
        if(platform==null || str==null)return null;
        if(str.equalsIgnoreCase(" - "))return null;

        //e.g. str = [![Untested](https://img.shields.io/badge/linux--amd64-Untested-lightgrey)](#)
        String result = str.substring(str.indexOf("[")+1);
        result = result.substring(result.indexOf("[")+1);

        String testResult = result.substring(0, result.indexOf("]"));

        result = result.substring(result.indexOf("(")+1);
        result = result.substring(result.indexOf("(")+1);

        String href = result.substring(0, result.indexOf(")"));
        if(href.equals("#"))href = null;

        return new TestResult(platform, EnumTestResult.fromString(testResult), href);
    }
}
