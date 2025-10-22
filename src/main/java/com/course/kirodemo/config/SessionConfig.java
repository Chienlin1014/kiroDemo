package com.course.kirodemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session 配置
 * 配置會話儲存、Cookie 設定和會話清理機制
 */
@Configuration
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 1800, // 30分鐘會話超時
    tableName = "SPRING_SESSION"
)
public class SessionConfig {

    /**
     * Cookie 序列化器配置
     * 設定會話 Cookie 的安全屬性
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        // Cookie 名稱
        serializer.setCookieName("SESSION");
        
        // Cookie 路徑
        serializer.setCookiePath("/");
        
        // HttpOnly 屬性，防止 XSS 攻擊
        serializer.setUseHttpOnlyCookie(true);
        
        // Secure 屬性（開發環境設為 false，生產環境應設為 true）
        serializer.setUseSecureCookie(false);
        
        // SameSite 屬性，防止 CSRF 攻擊
        serializer.setSameSite("Lax");
        
        // Cookie 最大存活時間（30分鐘）
        serializer.setCookieMaxAge(1800);
        
        return serializer;
    }
}