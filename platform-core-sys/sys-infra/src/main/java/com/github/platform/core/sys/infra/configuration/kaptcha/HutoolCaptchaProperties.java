package com.github.platform.core.sys.infra.configuration.kaptcha;

import cn.hutool.core.lang.generator.Generator;
import com.github.platform.core.cache.domain.constant.CacheConstant;
import com.github.platform.core.common.constant.PropertyConstant;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.standard.constant.SymbolConstant;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.awt.*;
import java.io.Serializable;

/**
 * <TODO>
 *
 * @Author: yxkong
 * @Date: 2025/3/27
 * @version: 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = PropertyConstant.DATA_KAPTCHA)
public class HutoolCaptchaProperties implements Serializable {
    public static final long DEFAULT_CAPTCHA_TIMEOUT = 5 * 60;
    /**
     * 验证码宽度
     */
    private int width = 100;
    /**
     * 验证码高度
     */
    private int height = 50;
    /**
     * 干扰线数量
     */
    private int interfereCount = 5;
    /**
     * 字体相关设置
     */
    private FontSettings font = new FontSettings();
    /**
     * 验证码文本生成器
     */
    private String generator = "default";
    /**
     * 验证码核心配置
     */
    private Code code = new Code();
    /**
     * 背景颜色设置
     */
    private BackgroundSettings background = new BackgroundSettings();
    private Cache cache = new Cache();
    private String type;
    public String getKey(String verifySeq){
        return  getKey(null,verifySeq);
    }
    public String getKey(String bizNo,String verifySeq){
        StringBuilder sb = new StringBuilder(this.cache.getPrefix());
        if (StringUtils.isNotEmpty(bizNo)){
            sb.append(bizNo).append(SymbolConstant.colon);
        }
        return  sb.append(verifySeq).toString();
    }
    /**
     * 文字透明度设置
     */
    @Data
    public static class FontSettings implements Serializable {
        /**
         * 字体名称
         */
        private String name = "Arial";
        /**
         * 字体大小
         */
        private int size = 30;
        /**
         * 字体样式
         * PLAIN 普通无装饰样式
         * BOLD 粗体样式
         * ITALIC 斜体样式
         */
        private int style = Font.BOLD;
        /**
         * 文字透明度
         */
        private float alpha = 1.0f;

    }

    @Data
    public static class BackgroundSettings implements Serializable {
        /**
         * 背景颜色
         */
        private String color = "255,255,255";

        public Color getColor() {
            if (StringUtils.isNotBlank(color)) {
                String[] rgb = color.split(",");
                if (rgb.length == 3) {
                    return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
                }
            }
            return Color.WHITE;
        }

    }
    @Data
    public static class Cache {
        /**验证码缓存前缀*/
        private String prefix = CacheConstant.captcha;
        /**验证码缓存时间，单位秒，默认3min*/
        private Long timeout = DEFAULT_CAPTCHA_TIMEOUT;
    }
    @Data
    public static class Code {
        /**
         * 验证码长度
         */
        private int length = 4;
        /**
         * 干扰类型（line/circle/shear）
         */
        private String interference = "shear";
        /**
         * 验证码类型（math/number/letter）
         */
        private String type = "math";
    }

}