package com.must.courseevaluation.dto;

import java.util.List;

/**
 * 课程AI总结DTO
 */
public class CourseSummaryDto {
    private String overall;      // 总体评价
    private String difficulty;   // 课程难度与作业量
    private String teaching;     // 教师授课风格
    private List<String> pros;   // 优点列表
    private List<String> cons;   // 缺点列表
    private String suggestion;   // 建议
    private String updatedAt;    // 更新时间
    private Integer reviewCount; // 评论数量

    public CourseSummaryDto() {
    }

    public CourseSummaryDto(String overall, String difficulty, String teaching, 
                           List<String> pros, List<String> cons, String suggestion) {
        this.overall = overall;
        this.difficulty = difficulty;
        this.teaching = teaching;
        this.pros = pros;
        this.cons = cons;
        this.suggestion = suggestion;
    }

    // Getters and Setters
    public String getOverall() {
        return overall;
    }

    public void setOverall(String overall) {
        this.overall = overall;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTeaching() {
        return teaching;
    }

    public void setTeaching(String teaching) {
        this.teaching = teaching;
    }

    public List<String> getPros() {
        return pros;
    }

    public void setPros(List<String> pros) {
        this.pros = pros;
    }

    public List<String> getCons() {
        return cons;
    }

    public void setCons(List<String> cons) {
        this.cons = cons;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
}

