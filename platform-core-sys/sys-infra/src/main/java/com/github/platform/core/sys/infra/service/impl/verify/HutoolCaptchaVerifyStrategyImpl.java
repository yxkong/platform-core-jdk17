package com.github.platform.core.sys.infra.service.impl.verify;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.RandomUtil;
import com.github.platform.core.cache.infra.service.ICacheService;
import com.github.platform.core.common.service.BaseServiceImpl;
import com.github.platform.core.common.utils.IdWorker;
import com.github.platform.core.standard.exception.InfrastructureException;
import com.github.platform.core.sys.domain.constant.CaptchaTypeEnum;
import com.github.platform.core.sys.domain.dto.VerifyCodeResult;
import com.github.platform.core.sys.domain.model.sms.VerifyEntity;
import com.github.platform.core.sys.domain.model.user.UserEntity;
import com.github.platform.core.sys.domain.service.VerifyStrategy;
import com.github.platform.core.sys.infra.configuration.kaptcha.HutoolCaptchaProperties;
import com.github.platform.core.sys.infra.constant.SysInfraResultEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * 图形验证策略
 *
 * @author: yxkong
 * @date: 2023/1/4 2:10 PM
 * @version: 1.0
 */
@Service("hutoolCaptchaVerifyStrategy")
@Slf4j
public class HutoolCaptchaVerifyStrategyImpl extends BaseServiceImpl implements VerifyStrategy {
    @Resource
    private HutoolCaptchaProperties properties;
    @Resource
    private IdWorker idWorker;
    @Resource
    private ICacheService cacheService;


    @Override
    public VerifyCodeResult getCode(UserEntity user) {
        String captchaTypeStr = properties.getCode().getType().toLowerCase();
        String interference = properties.getCode().getInterference();
        CaptchaTypeEnum captchaType = CaptchaTypeEnum.of(captchaTypeStr);
        // 获取基础配置
        int width = properties.getWidth();
        int height = properties.getHeight();
        int length = properties.getCode().getLength();
        int interfereCount = properties.getInterfereCount();

        // 创建验证码对象
        AbstractCaptcha captcha = createCaptchaByType(width, height, length, interfereCount, interference,captchaType);
        // 应用样式配置
        applyStyleSettings(captcha);
        String code = captcha.getCode();
        //在这里转成结果，屏蔽差异
        if (CaptchaTypeEnum.MATH.equals(captchaType)){
            code = String.valueOf((int) Calculator.conversion(code));
        }
        // 认证序列号
        String verifySeq = idWorker.nextId()+"";
        String key = properties.getKey(verifySeq);
        cacheService.set(key,code,properties.getCache().getTimeout());
        if (log.isDebugEnabled()) {
            log.debug("缓存key 为：{} val 为：{}", key, code);
        }

        return VerifyCodeResult.builder()
                .verifySeq(verifySeq)
                .images(captcha.getImageBase64())
                .build();
    }

    private CodeGenerator getGenerator(CaptchaTypeEnum captchaType, Integer length) {
        return switch (captchaType.getType().toLowerCase()) {
            case "math" -> new MathGenerator(length);
            case "number" -> new RandomGenerator(RandomUtil.BASE_NUMBER, length);
            case "letter" -> new RandomGenerator(RandomUtil.BASE_CHAR, length);
            default -> new RandomGenerator(length);
        };
    }

    /**
     * 生成AbstractCaptcha
     * @param width 宽度
     * @param height 高度
     * @param length 验证码长度
     * @param count 干扰线个数或宽度
     * @param interference 干扰类型类型
     * @param captchaType 验证码类型
     * @return
     */
    private AbstractCaptcha createCaptchaByType(int width, int height, int length, int count, String interference,CaptchaTypeEnum captchaType) {
        return switch (interference) {
            case "circle" ->
                //圆圈干扰验证码,count 干扰元素
                    CaptchaUtil.createCircleCaptcha(width, height, getGenerator(captchaType, length), count);
            case "shear" ->
                //扭曲干扰验证码,1干扰线宽度
                    CaptchaUtil.createShearCaptcha(width, height, getGenerator(captchaType, length), 2);
            default ->
                //线段干扰的验证码
                    CaptchaUtil.createLineCaptcha(width, height, getGenerator(captchaType, length), count);
        };
    }
    private void applyStyleSettings(AbstractCaptcha captcha) {
        // 设置字体
        HutoolCaptchaProperties.FontSettings fontSettings = properties.getFont();
        Font font = new Font(fontSettings.getName(), fontSettings.getStyle(), fontSettings.getSize());
        captcha.setFont(font);
        // 设置背景
        captcha.setBackground(properties.getBackground().getColor());
    }
    @Override
    public void verify(VerifyEntity verifyEntity)throws InfrastructureException {
        String verifySeq = verifyEntity.getVerifySeq();

        String key = properties.getKey(null, verifySeq);
        Object o = cacheService.get(key);
        if(log.isDebugEnabled()){
            log.debug("获取redis的key为{} val为{}",key,o);
        }

        if (null == o){
            if(log.isWarnEnabled()){
                log.warn("未获取到流水号：{} 对应的值",verifySeq);
            }
            throw exception(SysInfraResultEnum.VERIFY_CODE_EXPIRE);
        }
        try {
            String redisCode = (String)o;
            if (!redisCode.equalsIgnoreCase(verifyEntity.getVerifyCode())){
                throw exception(SysInfraResultEnum.VERIFY_ERROR);
            }
        } finally {
            cacheService.del(key);
        }
    }

    /**
     * 验证收尾
     *
     * @param verifyEntity
     * @return
     */
    @Override
    public void finish(VerifyEntity verifyEntity) {
        //
    }

}
