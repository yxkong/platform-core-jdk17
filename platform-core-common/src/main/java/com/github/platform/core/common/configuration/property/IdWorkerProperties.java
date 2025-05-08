package com.github.platform.core.common.configuration.property;

import com.github.platform.core.common.constant.PropertyConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yxkong
 */
@Setter
@Component
@ConfigurationProperties(prefix =  PropertyConstant.DATA_SNOWFLAKE)
public class IdWorkerProperties {
    @Getter
    private List<String> cluster;
    private int skip;

    public int getSkip() {
        if (skip == 0){
            return 1;
        }
        return skip;
    }

}
