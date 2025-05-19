package com.github.platform.core.sms.application.executor.impl;

import com.github.platform.core.common.service.BaseExecutor;
import com.github.platform.core.common.utils.*;
import com.github.platform.core.sms.application.executor.ISmsExecutor;
import com.github.platform.core.sms.domain.context.SendSmsContext;
import com.github.platform.core.sms.domain.context.SysSmsListQueryContext;
import com.github.platform.core.sms.domain.context.SysSmsLogContext;
import com.github.platform.core.sms.domain.dto.SysSmsListDto;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateDto;
import com.github.platform.core.sms.domain.entity.SendSmsEntity;
import com.github.platform.core.sms.domain.entity.SendSmsResultEntity;
import com.github.platform.core.sms.domain.entity.SmsSpTemplateEntity;
import com.github.platform.core.sms.domain.gateway.ISysSmsLogGateway;
import com.github.platform.core.sms.domain.gateway.ISysSmsTemplateGateway;
import com.github.platform.core.sms.domain.gateway.ISysSmsListGateway;
import com.github.platform.core.sms.infra.convert.SmsInfraConvert;
import com.github.platform.core.sms.infra.service.ISmsRouterService;
import com.github.platform.core.sms.infra.service.ISmsService;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.constant.StatusEnum;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.standard.exception.CommonException;
import com.github.platform.core.standard.exception.ConfigRuntimeException;
import com.github.platform.core.standard.util.ResultBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;

/**
 *
 * @author: yxkong
 * @date: 2023/3/1 2:56 PM
 * @version: 1.0
 */
@Service
@Slf4j
public class SmsExecutor extends BaseExecutor implements ISmsExecutor {
    // 依赖注入资源
    // 短信服务提供商集合
    @Resource
    private ObjectProvider<ISmsService> sendServices;
    // 短信日志网关
    @Resource
    private ISysSmsLogGateway sysSmsLogGateway;
    // 短信模板网关
    @Resource
    private ISysSmsTemplateGateway smsTemplateGateway;
    // 短信白名单网关
    @Resource
    private ISysSmsListGateway smsListGateway;
    // 短信路由服务
    @Resource
    private ISmsRouterService routerService;
    // 短信对象转换器
    @Resource
    private SmsInfraConvert convert;
    // ID生成器
    @Resource
    private IdWorker idWorker;

    /**
     * 发送短信主方法
     * @param context 短信发送上下文
     * @return 发送结果
     */
    @Override
    public ResultBean sendSms(SendSmsContext context) {
        try {
            /* ① 幂等校验 */
            validateMessageId(context.getMsgId(), context);

            /* ② 查找并验证模板 */
            SysSmsTemplateDto smsTemplate = getValidTemplate(context.getTempNo());

            /* ③ 路由到可用的厂商模板 */
            SmsSpTemplateEntity spTemplate = routeToProviderTemplate(smsTemplate);

            /* ④ 获取对应厂商的实现,需要各自实现 */
            ISmsService smsService = findSmsService(spTemplate.getProvider());

            /* ⑤ 解析短信内容 */
            String content = parseContent(smsTemplate.getContent(), context.getParams());

            /* ⑥ 保存短信发送日志 */
            SysSmsLogContext smsLog = saveSmsLog(context, spTemplate.getProNo(), content);

            /* ⑦ 执行短信发送 */
            SendSmsResultEntity sendResult = executeSend(context, spTemplate, content, smsService);

            /* ⑧ 更新发送结果 */
            updateSendResult(smsLog, sendResult);

            return ResultBeanUtil.success();
        } catch (CommonException e) {
            log.warn("业务异常 occurred while sending SMS", e);
            return ResultBeanUtil.result(e);
        } catch (Exception e) {
            log.error("发送短信时发生未预期错误", e);
            return ResultBeanUtil.fail(e.getMessage());
        }
    }

    /**
     * 幂等校验
     * @param msgId 消息ID
     * @param context 短信上下文
     */
    private void validateMessageId(String msgId, SendSmsContext context) {
        if (StringUtils.isNotEmpty(msgId)) {
            throw new IllegalArgumentException("消息ID不能为空");
        }

        if (repeat(msgId)) {
            log.warn("检测到重复消息ID，忽略请求。上下文: {}", JsonUtils.toJson(context));
            throw exception(ResultStatusEnum.REPEAT);
        }
    }

    /**
     * 获取并验证模板有效性
     * @param tempNo 模板编号
     * @return 有效的模板对象
     */
    private SysSmsTemplateDto getValidTemplate(String tempNo) {
        SysSmsTemplateDto template = smsTemplateGateway.findByTempNo(tempNo);
        if (template == null || !template.isValid()) {
            throw new ConfigRuntimeException(
                    ResultStatusEnum.CONFIG_ERROR.getStatus(),
                    "未找到有效的模板，请核查");
        }
        return template;
    }

    /**
     * 路由到服务商模板
     * @param smsTemplate 短信模板
     * @return 服务商模板实体
     */
    private SmsSpTemplateEntity routeToProviderTemplate(SysSmsTemplateDto smsTemplate) {
        SmsSpTemplateEntity spTemplate = routerService.router(smsTemplate);
        if (spTemplate == null) {
            throw new ConfigRuntimeException(
                    ResultStatusEnum.CONFIG_ERROR.getStatus(),
                    "未找到模板对应的有效厂商");
        }
        return spTemplate;
    }

    /**
     * 查找短信服务实现
     * @param provider 服务提供商
     * @return 短信服务实例
     */
    private ISmsService findSmsService(String provider) {
        return sendServices.stream()
                .filter(service -> service.support(provider))
                .findFirst()
                .orElseThrow(() -> new ConfigRuntimeException(
                        ResultStatusEnum.CONFIG_ERROR.getStatus(),
                        "找不到对应服务商实现: " + provider));
    }

    /**
     * 解析短信模板内容
     * @param template 模板内容
     * @param params 参数
     * @return 解析后的内容
     */
    private String parseContent(String template, Map<String, Object> params) {
        try {
            return PlaceholderUtil.replace(template, params);
        } catch (Exception e) {
            throw exception(
                    ResultStatusEnum.PARAM_ERROR.getStatus(),
                    "解析短信内容失败: " + e.getMessage());
        }
    }

    /**
     * 保存短信日志
     * @param context 发送上下文
     * @param proNo 提供商编号
     * @param content 短信内容
     * @return 日志上下文
     */
    private SysSmsLogContext saveSmsLog(SendSmsContext context, String proNo, String content) {
        SysSmsLogContext smsLog = convert.of(
                context, proNo, content, StatusEnum.OFF.getStatus(), 0);
        sysSmsLogGateway.insert(smsLog);
        log.debug("短信日志已保存，ID: {}", smsLog.getId());
        return smsLog;
    }

    /**
     * 执行短信发送
     * @param context 发送上下文
     * @param spTemplate 服务商模板
     * @param content 短信内容
     * @param smsService 短信服务
     * @return 发送结果
     */
    private SendSmsResultEntity executeSend(SendSmsContext context,
                                            SmsSpTemplateEntity spTemplate,
                                            String content,
                                            ISmsService smsService) {
        // 组装发送短信上下文
        SendSmsEntity smsSend = convert.of(context, spTemplate, content);
        if (log.isDebugEnabled()){
            log.debug("短信发送实体: {}", JsonUtils.toJson(smsSend));
        }
        // 非生产环境只能发送白名单中的
        if (!ApplicationContextHolder.isProd() && !isWhiteList(smsSend.getMobile())) {
            String msgId = idWorker.bizNo();
            log.info("当前手机号:{} 不在白名单内,构建模拟成功消息ID: {}",
                    context.getMobile(), msgId);
            return SendSmsResultEntity.builder()
                    .status(1)
                    .msgId(msgId)
                    .build();
        }
        if (ApplicationContextHolder.isProd()){
            long count = smsListGateway.findListByCount(SysSmsListQueryContext.builder().mobile(context.getMobile()).type(StatusEnum.ON.getStatus()).status(StatusEnum.ON.getStatus()).build());
            if (count > 0){
                return SendSmsResultEntity.builder().status(-2).build();
            }
        }
        try {
            SendSmsResultEntity result = smsService.sendSms(smsSend);
            if (log.isDebugEnabled()){
                log.debug("短信发送结果: {}", JsonUtils.toJson(result));
            }

            return result;
        } catch (Exception e) {
            log.error("短信发送失败，服务商: {}", spTemplate.getProvider(), e);
            return SendSmsResultEntity.builder().status(-1).errorMsg(e.getMessage()).build();
        }
    }

    /**
     * 更新发送结果
     * @param smsLog 日志上下文
     * @param sendResult 发送结果
     */
    private void updateSendResult(SysSmsLogContext smsLog, SendSmsResultEntity sendResult) {
        SysSmsLogContext updateDo = SysSmsLogContext.builder()
                .id(smsLog.getId())
                .msgId(sendResult.getMsgId())
                .status(sendResult.getStatus())
                .remark(buildRemark(smsLog.getRemark(), sendResult.getErrorMsg()))
                .build();
        sysSmsLogGateway.update(updateDo);
        log.debug("短信日志已更新，ID: {}", smsLog.getId());
    }

    /**
     * 构建备注信息
     * @param existingRemark 现有备注
     * @param newMessage 新消息
     * @return 组合后的备注
     */
    private String buildRemark(String existingRemark, String newMessage) {
        return StringUtils.isNotBlank(existingRemark)
                ? existingRemark + ", 发送消息:" + newMessage
                : "发送消息:" + newMessage;
    }

    /**
     * 检查手机号是否在白名单
     * @param mobile 手机号
     * @return 是否在白名单
     */
    private boolean isWhiteList(String mobile) {
        return smsListGateway.existMobile(mobile);
    }

    /**
     * 检查消息ID是否重复
     * @param msgId 消息ID
     * @return 是否重复
     */
    private boolean repeat(String msgId) {
        return sysSmsLogGateway.findByMsgId(msgId) != null;
    }
}


