package com.rohit.MFAnalyzer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("eod")
public class MyProperties {
    private String security_name;
    private String data_dir;
    private int date_index;
    private int eod_price_index;
    private String date_format;
}
