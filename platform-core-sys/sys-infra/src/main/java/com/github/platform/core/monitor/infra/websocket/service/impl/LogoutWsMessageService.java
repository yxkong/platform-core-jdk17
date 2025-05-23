package com.github.platform.core.monitor.infra.websocket.service.impl;

import com.github.platform.core.auth.entity.LoginUserInfo;
import com.github.platform.core.auth.service.ITokenService;
import com.github.platform.core.cache.domain.constant.CacheConstant;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.monitor.domain.ws.InMessage;
import com.github.platform.core.monitor.domain.ws.OutMessage;
import com.github.platform.core.monitor.infra.websocket.WebSocketSessionManager;
import com.github.platform.core.monitor.domain.constant.WsConstant;
import com.github.platform.core.monitor.infra.websocket.service.IWsMessageService;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.Resource;
import java.io.IOException;

/**
 * 踢出消息处理
 * @author: yxkong
 * @date: 2023/6/13 11:13 AM
 * @version: 1.0
 */
@Service("wsLogoutWsMessageService")
@Slf4j
public class LogoutWsMessageService implements IWsMessageService {
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private WebSocketSessionManager sessionManager;
    @Resource(name = CacheConstant.sysTokenService)
    private ITokenService tokenService;
    @Resource(name = "wsChatWsMessageService")
    private IWsMessageService messageService;

    @Override
    public boolean executor(InMessage wsMessage, boolean join) {
        if (log.isWarnEnabled()){
            log.warn("用户：{} 要踢出：{}",wsMessage.getFromUser(),wsMessage.getToUser());
        }
        LoginUserInfo loginInfo = wsMessage.getLoginInfo();
        WebSocketSession session = sessionManager.get(loginInfo.getWebSocketKey());
        if (null == session && join){
            /**不在本机,广播出去*/
            stringRedisTemplate.convertAndSend(WsConstant.BIZ_TYPE_LOGOUT, JsonUtils.toJson(wsMessage));
            return false;
        }
        try {
            OutMessage outMessage = OutMessage.builder().bizType(WsConstant.BIZ_TYPE_LOGOUT).status(ResultStatusEnum.TOKEN_INVALID.getStatus()).message("踢出成功！").build();
            session.sendMessage(new TextMessage(JsonUtils.toJson(outMessage)));
            /** 在本机，直接执行 */
            session.close();
            //清除上次token的登陆信息,TODO 判断只清理当前还是对应用户
            tokenService.expireByToken(loginInfo.getToken());
            if (wsMessage.getFromUser().equals(wsMessage.getToUser())){
                if (log.isInfoEnabled()){
                    log.info("用户：{} 退出,断开socket成功",wsMessage.getFromUser());
                }
            } else {
                if (log.isInfoEnabled()){
                    log.info("用户：{} 踢出：{} 成功",wsMessage.getFromUser(),wsMessage.getToUser());
                }
                //踢出用户以后需要给踢出者回复消息
                String loginInfoStr = tokenService.getLoginInfoStr(wsMessage.getLoginInfo().getTenantId(), wsMessage.getFromUser());
                if (log.isInfoEnabled()){
                    log.info("踢出者用户信息：{}",loginInfoStr);
                }
                InMessage rsp = InMessage.builder().fromUser("robot").toUser(wsMessage.getFromUser()).text("踢出成功！").build();
                rsp.setLoginInfoStr(loginInfoStr);
                messageService.executor(rsp,true);
            }
            return true;
        } catch (IOException e) {
            log.error("",e);
            return false;
        }
    }
}
