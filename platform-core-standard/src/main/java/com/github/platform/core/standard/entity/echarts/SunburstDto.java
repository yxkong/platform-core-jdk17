package com.github.platform.core.standard.entity.echarts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 旭日图
 * @Author: yxkong
 * @Date: 2025/4/3
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunburstDto {
    private String name;
    private Integer value;
    private List<SunburstDto> children;
}
