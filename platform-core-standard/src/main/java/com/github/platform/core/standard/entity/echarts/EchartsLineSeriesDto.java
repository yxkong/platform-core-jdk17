package com.github.platform.core.standard.entity.echarts;

import lombok.Data;

import java.util.List;

/**
 * 折线图Series数据
 * @Author: yxkong
 * @Date: 2025/4/19
 * @version: 1.0
 */
@Data
public class EchartsLineSeriesDto {
    private String name;
    private String type;
    private String stack;
    private List<Integer> data;

    public EchartsLineSeriesDto(String name, List<Integer> data) {
        this(name,"line","total",data);
    }

    public EchartsLineSeriesDto(String name, String stack, List<Integer> data) {
        this(name,"line",stack,data);
    }

    public EchartsLineSeriesDto(String name, String type, String stack, List<Integer> data) {
        this.name = name;
        this.type = type;
        this.stack = stack;
        this.data = data;
    }
}
