package com.github.platform.core.standard.entity.echarts;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 旭日图
 * @Author: yxkong
 * @Date: 2025/4/3
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SunburstDto extends EchartsKVDto {
    private List<SunburstDto> children;
}
