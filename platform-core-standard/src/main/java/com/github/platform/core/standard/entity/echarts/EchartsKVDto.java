package com.github.platform.core.standard.entity.echarts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * echarts kv 数据
 * @Author: yxkong
 * @Date: 2025/4/9
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EchartsKVDto {
    private String name;
    private Integer value;
    private EchartsItemStyle itemStyle;

    public EchartsKVDto(String name, Integer value) {
        this.name = name;
        this.value = value;
    }
}
