package com.rohit.MFAnalyzer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties
public class MyProperties {

    @Data
    public static class FileProperty {
        private String security_name;
        private String data_dir;
        private int date_index;
        private int eod_price_index;
        private String date_format;
    }

    private List<FileProperty> file_properties;
    private String file_persistence_service;
}
