package com.macro.mall.portal.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
public class AppConfig {
    @Value("${app.blockIOS}")
    private Boolean blockIOS;
}
