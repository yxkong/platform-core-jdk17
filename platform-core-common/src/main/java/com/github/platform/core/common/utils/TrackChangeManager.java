package com.github.platform.core.common.utils;

import com.github.platform.core.common.annotation.TrackChange;
import com.github.platform.core.common.entity.TrackChangeRecord;
import com.github.platform.core.standard.constant.SymbolConstant;
import com.github.platform.core.standard.entity.BaseEntity;
import com.github.platform.core.standard.util.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 追踪工具
 * @author: yxkong
 * @date: 2024/6/7 14:05
 * @version: 1.0
 */
@Slf4j
public class TrackChangeManager {

    private List<TrackChangeRecord> changes ;
    /**源实体*/
    private Object original;
    /**修改后的实体*/
    private Object modified;
    private Integer level;
    /**是否新增*/
    private Boolean isNew;

    public TrackChangeManager(Object original, Object modified) {
        this.changes = new ArrayList<>();
        this.original = original;
        this.modified = modified;
        this.level = 1;
        initChanges(Objects.nonNull(original) ? original.getClass() : modified.getClass());
    }

    private void initChanges(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        // 判断 original 是否为 null，如果为 null 则表示新增操作
        isNew = original == null;
        for (Field field : clazz.getDeclaredFields()) {
            TrackChange trackChange = field.getAnnotation(TrackChange.class);
            if (trackChange == null) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object originalValue = isNew ? null : field.get(this.original);
                Object modifiedValue = field.get(this.modified);
                // 对于非合并字段，如果修改值为null或空字符串则跳过；合并字段则不跳过，便于后续合并判断
                if ((modifiedValue == null || "".equals(modifiedValue.toString()))
                        && StringUtils.isEmpty(trackChange.merge())) {
                    continue;
                }
                // 判断是否需要记录变化：
                // 如果是新增操作，或者原值和新值不等，或者该字段为合并对象（merge = "ignore"）则认为有变化
                boolean hasChanged = isNew || !Objects.equals(originalValue, modifiedValue)
                        || "ignore".equals(trackChange.merge());
                if (hasChanged) {
                    TrackChangeRecord record = new TrackChangeRecord(
                            field.getName(),
                            trackChange.compare(),
                            trackChange.merge(),
                            trackChange.separator(),
                            originalValue,
                            modifiedValue,
                            trackChange.remark(),
                            trackChange.dateFormat(),
                            trackChange.sort()
                    );
                    changes.add(record);
                }
            } catch (Exception e) {
                log.error("TrackChangeManager initChanges error for field " + field.getName(), e);
            }
        }
        // 递归处理父类，但只处理有限层级
        if (shouldContinueWithSuperclass(clazz)) {
            initChanges(clazz.getSuperclass());
        }
    }

    private boolean shouldContinueWithSuperclass(Class<?> clazz) {
        return !Objects.equals(clazz.getSuperclass(), Object.class) &&
                !Objects.equals(clazz.getSuperclass(), Serializable.class) &&
                !Objects.equals(clazz.getSuperclass(), BaseEntity.class) &&
                level <= 2;
    }

    /**
     * 获取改变结果
     * @param prefixSplit
     * @return
     */
    public String getChangeResult(String prefixSplit) {
        if (this.changes.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        // 提前缓存需要的 merge 数据
        Map<String, TrackChangeRecord> mergeCache = changes.stream()
                .filter(x -> StringUtils.isNotEmpty(x.getMerge()))
                .collect(Collectors.toMap(TrackChangeRecord::getFieldName, record -> record));

        changes.stream()
                .filter(s -> !Objects.equals(s.getMerge(), "ignore"))
                .sorted(Comparator.comparingInt(TrackChangeRecord::getSort))
                .forEach(s -> {
                    Object originalValue = s.getOriginalValue();
                    Object modifiedValue = s.getModifiedValue();

                    // 处理日期格式化
                    if (StringUtils.isNotEmpty(s.getDateFormat())) {
                        originalValue = LocalDateTimeUtil.parseDateTime(originalValue.toString(), s.getDateFormat());
                        modifiedValue = LocalDateTimeUtil.parseDateTime(modifiedValue.toString(), s.getDateFormat());
                    }

                    if (isNew) {
                        appendNewChange(sb, prefixSplit, s, modifiedValue);
                    } else {
                        appendExistingChange(sb, prefixSplit, s, originalValue, modifiedValue, mergeCache);
                    }
                });
        return !sb.isEmpty() ? sb.substring(prefixSplit.length()) : sb.toString();
    }

    private void appendNewChange(StringBuilder sb, String prefixSplit, TrackChangeRecord s, Object modifiedValue) {
        if (Objects.nonNull(modifiedValue)) {
            sb.append(prefixSplit)
                    .append(s.getRemark())
                    .append(SymbolConstant.COLON)
                    .append(modifiedValue);
        }
    }

    private void appendExistingChange(StringBuilder sb, String prefixSplit, TrackChangeRecord s,
                                      Object originalValue, Object modifiedValue, Map<String, TrackChangeRecord> mergeCache) {
        if (StringUtils.isNotEmpty(s.getMerge())) {
            TrackChangeRecord merge = mergeCache.get(s.getMerge());
            if (merge != null) {
                // 如果任一字段变化，则认为合并后有变化
                if (hasMergedChanged(s, merge)) {
                    sb.append(prefixSplit)
                            .append(s.getRemark())
                            .append("由【")
                            .append(s.getOriginalValue())
                            .append(s.getSeparator())
                            .append(merge.getOriginalValue())
                            .append("】变为：【")
                            .append(s.getModifiedValue())
                            .append(s.getSeparator())
                            .append(merge.getModifiedValue())
                            .append("】");
                }
            } else {
                // 如果合并字段记录不存在，则单独处理当前字段变化
                appendNormal(sb, prefixSplit, s, originalValue, modifiedValue);
            }
        } else {
            appendNormal(sb, prefixSplit, s, originalValue, modifiedValue);
        }
    }

    private static void appendNormal(StringBuilder sb, String prefixSplit, TrackChangeRecord s, Object originalValue, Object modifiedValue) {
        if (s.isCompare()) {
            sb.append(prefixSplit)
                    .append(s.getRemark())
                    .append("由【")
                    .append(originalValue)
                    .append("】变为：【")
                    .append(modifiedValue)
                    .append("】");
        } else {
            if (modifiedValue != null) {
                sb.append(prefixSplit)
                        .append(s.getRemark())
                        .append(SymbolConstant.COLON)
                        .append(modifiedValue);
            }
        }
    }


    private boolean hasMergedChanged(TrackChangeRecord s, TrackChangeRecord merge) {
        return !Objects.equals(s.getOriginalValue(), s.getModifiedValue()) ||
                !Objects.equals(merge.getOriginalValue(), merge.getModifiedValue());
    }
}
