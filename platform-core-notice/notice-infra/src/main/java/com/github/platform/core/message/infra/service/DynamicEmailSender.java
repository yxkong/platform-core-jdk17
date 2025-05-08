//package com.github.platform.core.message.infra.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.mail.MailPreparationException;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Properties;
//
///**
// * 动态邮件发送
// * @Author: yxkong
// * @Date: 2025/3/10
// * @version: 1.0
// */
//@Service
//public class DynamicEmailSender {
//    /**
//     * 动态发送邮件
//     * @param host SMTP服务器地址
//     * @param port SMTP端口
//     * @param username 邮箱账号
//     * @param password 邮箱密码/授权码
//     * @param isHtml 是否html true表示支持HTML
//     */
//    public void sendEmail(String host, int port, String username, String password, List<String> to, List<String> cc, List<String> bcc, String subject, String content, boolean isHtml) {
//        // 创建新的发送实例确保线程安全
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(host);
//        mailSender.setPort(port);
//        mailSender.setUsername(username);
//        mailSender.setPassword(password);
//
//        // 动态配置协议和安全设置
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.ssl.enable", "true");
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        mailSender.setJavaMailProperties(props);
//        // 构建并发送邮件
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(username);
//            // 设置收件人
//            helper.setTo(to.toArray(new String[0]));
//            // 设置抄送
//            if (cc != null && !cc.isEmpty()) {
//                helper.setCc(cc.toArray(new String[0]));
//            }
//            // 设置密送
//            if (bcc != null && !bcc.isEmpty()) {
//                helper.setBcc(bcc.toArray(new String[0]));
//            }
//            helper.setSubject(subject);
//            helper.setText(content, isHtml);
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new MailPreparationException("邮件构建失败", e);
//        }
//    }
//}
