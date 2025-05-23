package com.github.platform.core.web.util;

import com.github.platform.core.common.utils.IPUtil;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.standard.constant.HeaderConstant;
import com.github.platform.core.standard.constant.SymbolConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author: yxkong
 * @date: 2021/5/12 12:19 下午
 * @version: 1.0
 */
@Slf4j
public class WebUtil extends IPUtil {

    public static String getIpHost(HttpServletRequest request) {
        /**
         * 在转发的时候，将原客户的请求ip放入到header中 字段requestSourceIp
         * 如果有该requestSourceIp，直接使用这个ip，（慎用）
         */
        String requestIp = request.getHeader("requestSourceIp");
        if (StringUtils.isEmpty(requestIp)) {
            requestIp = request.getHeader("reuestSourceIp");
        }
        if (!StringUtils.isEmpty(requestIp)) {
            if (requestIp.contains(SymbolConstant.COMMA)) {
                return requestIp.split(SymbolConstant.COMMA)[0];
            }
            return requestIp;
        }
        return getIpAddress(request);
    }
    public static String getIpAddress(){
        return getIpAddress(RequestHolder.getRequest());
    }

    public static String getIpAddress(HttpServletRequest request) {

        String ip = request.getHeader(HeaderConstant.IP_HEADER_X_REQUESTED_FOR);
        if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
            //这是一个 Squid 开发的字段，只有在通过了HTTP代理或者负载均衡服务器时才会添加该项,多个以逗号分隔
            ip = request.getHeader(HeaderConstant.IP_HEADER_X_FORWARDED_FOR);
        }
        if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
            //apache 服务代理ip
            ip = request.getHeader(HeaderConstant.IP_HEADER_PROXY_CLIENT_IP);
        }
        if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
            //weblogic 服务代理
            ip = request.getHeader(HeaderConstant.IP_HEADER_WL_PROXY_CLIENT_IP);
        }
        if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HeaderConstant.IP_HEADER_HTTP_CLIENT_IP);
        }
        if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HeaderConstant.IP_HEADER_HTTP_X_FORWARDED_FOR);
        }
        if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals(HeaderConstant.IPV6_DEFAULT_LOCAL) || ip.equals(HeaderConstant.IPV4_DEFAULT_LOCAL)) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    log.error("获取localhost ip is error", e);
                }
                ip = inet.getHostAddress();
            }
        }

        if (ip != null && ip.contains(SymbolConstant.COMMA)) {
            String[] ips = ip.split(SymbolConstant.COMMA);
            for (int index = 0; index < ips.length; index++) {
                String strIp = ips[index];
                if (!(HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 获取客户端的请求ip
     *
     * @param request
     * @return
     */
    public static String getIpAddr(ServerHttpRequest request) {
        /**
         * 在转发的时候，将原客户的请求ip放入到header中 字段reuestSourceIp
         * 如果有该reuestSourceIp，直接使用这个ip，（慎用）
         */
        try {
            HttpHeaders httpHeaders = request.getHeaders();
            String requestIp = httpHeaders.getFirst(HeaderConstant.IP_HEADER_REQUEST_SOURCE_IP);
            if (!StringUtils.isEmpty(requestIp)) {
                if (requestIp.contains(SymbolConstant.COMMA)) {
                    return requestIp.split(SymbolConstant.COMMA)[0];
                }
                return requestIp;
            }
            String ip = httpHeaders.getFirst(HeaderConstant.IP_HEADER_X_FORWARDED_FOR);
            if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
                ip = httpHeaders.getFirst(HeaderConstant.IP_HEADER_PROXY_CLIENT_IP);
            }
            if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
                ip = httpHeaders.getFirst(HeaderConstant.IP_HEADER_WL_PROXY_CLIENT_IP);
            }
            if (StringUtils.isEmpty(ip) || HeaderConstant.IP_UNKNOWN.equalsIgnoreCase(ip)) {
                InetSocketAddress inetSocketAddress = request.getRemoteAddress();
                ip = inetSocketAddress.getHostString();
            }
            if (StringUtils.isEmpty(ip)) {
                return HeaderConstant.IPV6_DEFAULT_LOCAL;
            }
            return ip;
        } catch (Exception e) {
            log.error("[网关][getIpAddr]发生异常", e);
        }
        return HeaderConstant.IPV6_DEFAULT_LOCAL;
    }
}