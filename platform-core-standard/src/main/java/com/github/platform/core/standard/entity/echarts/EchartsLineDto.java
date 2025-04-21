package com.github.platform.core.standard.entity.echarts;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

/**
 * echarts线性数据
 * @Author: yxkong
 * @Date: 2025/4/9
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class EchartsLineDto<T> {
    private String title;
    private List<String> legendData;
    private List<String> xAxisData;
    private List<T> yAxisData;
}
