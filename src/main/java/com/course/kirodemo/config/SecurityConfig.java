package com.course.kirodemo.config;

import com.course.kirodemo.security.CustomAuthenticationProvider;
import com.course.kirodemo.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Spring Security 配置
 * 配置認證和授權規則、會話管理、CSRF 保護和安全標頭
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                         CustomAuthenticationProvider customAuthenticationProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationProvider = customAuthenticationProvider;
    }

    /**
     * 密碼編碼器 Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 認證成功處理器
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/todos");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    /**
     * 認證失敗處理器
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl("/login?error=true");
        return handler;
    }

    /**
     * HTTP 會話事件發布器，用於會話管理
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 會話註冊表，用於追蹤活動會話
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 認證管理器配置
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authenticationManagerBuilder
            .authenticationProvider(customAuthenticationProvider)
            .userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
        
        return authenticationManagerBuilder.build();
    }

    /**
     * 安全過濾器鏈配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 授權配置
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/register", "/h2-console/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/error/**").permitAll()
                .anyRequest().authenticated()
            )
            
            // 表單登入配置
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            
            // 登出配置
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "SESSION")
                .clearAuthentication(true)
                .permitAll()
            )
            
            // 會話管理配置
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // 每個使用者最多一個會話
                .maxSessionsPreventsLogin(false) // 新登入會踢掉舊會話
                .expiredUrl("/login?expired=true")
                .sessionRegistry(sessionRegistry())
                .and()
                .sessionFixation().migrateSession() // 防止會話固定攻擊
                .invalidSessionUrl("/login?invalid=true")
            )
            
            // CSRF 保護配置
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // H2 Console 不需要 CSRF 保護
            )
            
            // 安全標頭配置
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // 允許 H2 Console 使用 iframe
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
            
            // 記住我功能（可選）
            .rememberMe(remember -> remember
                .key("todoapp-remember-me")
                .tokenValiditySeconds(86400) // 24小時
                .userDetailsService(customUserDetailsService)
            );

        return http.build();
    }
}