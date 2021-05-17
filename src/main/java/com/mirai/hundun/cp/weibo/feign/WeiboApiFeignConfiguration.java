package com.mirai.hundun.cp.weibo.feign;



import org.springframework.context.annotation.Bean;

import feign.Logger;

public class WeiboApiFeignConfiguration {

    @Bean
    Logger.Level level() {
        return Logger.Level.BASIC;
    }
    
}
