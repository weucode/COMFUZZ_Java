package com.jvmfuzzing.apigetter;

public class TestcaseInfo{
    private int id;
    private String testcaseContext;
    private int sourceTestcaseId;
    private int mutationMethod;
    private int mutationTimes;

    public TestcaseInfo(int id, String testcaseContext, int mutationTimes){
        this.id = id;
        this.testcaseContext = testcaseContext;
        this.sourceTestcaseId = 0;
        this.mutationMethod = 0;
        this.mutationTimes = mutationTimes;
    }

    public TestcaseInfo(String testcaseContext, int sourceTestcaseId, int mutationMethod, int mutationTimes){
        this.id = 0;
        this.testcaseContext = testcaseContext;
        this.sourceTestcaseId = sourceTestcaseId;
        this.mutationMethod = mutationMethod;
        this.mutationTimes = mutationTimes;
    }

    public int getId(){
        return id;
    }

    public String getTestcase(){
        return testcaseContext;
    }

    public int getSourceTestcaseId(){
        return sourceTestcaseId;
    }

    public int getMutationMethod(){
        return mutationMethod;
    }

    public int getMutationTimes(){
        return mutationTimes;
    }
}