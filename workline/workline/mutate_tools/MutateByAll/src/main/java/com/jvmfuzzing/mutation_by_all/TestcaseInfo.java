package com.jvmfuzzing.mutation_by_all;


public class TestcaseInfo{
    private int id;
    private String testcase_context;
    private int sourceFun_id;
    private int sourceTestcase_id;
    private int mutation_method;
    private int mutation_times;
    private int interesting_times;
    private int probability;

    public TestcaseInfo(int id, String testcase_context, int sourceTestcase_id, int mutation_times, int interesting_times, int probability){
        this.id = id;
        this.testcase_context = testcase_context;
        this.sourceFun_id = sourceTestcase_id;
        this.sourceTestcase_id = sourceTestcase_id;
        this.mutation_times = mutation_times;
        this.interesting_times = interesting_times;
        this.probability = probability;
    }

    public void setMutationMethod(int methodType){
        this.mutation_method = methodType;
    }

    public void plusMutationTime(){
        this.mutation_times++;
    }

    public int getId(){
        return this.id;
    }

    public String getTestcaseContent(){
        return this.testcase_context;
    }

    public int getSourceId(){
        return this.sourceFun_id;
    }

    public int getMutationMethod(){
        return this.mutation_method;
    }

    public int getMutationTimes(){
        return this.mutation_times;
    }

    public int getInterestingTimes(){
        return this.interesting_times;
    }

    public int getProbability(){
        return this.probability;
    }

    // public void setProbability(int prob){
    //     this.probability = prob;
    // }
}
