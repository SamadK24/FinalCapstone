package com.aurionpro.config;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
    	Map<String, String> config = Map.of(
                "cloud_name", "dymehx7ma",
                "api_key", "889284836776239",
                "api_secret", "3QElnY8fuXyFyLdHlCr6CgBRMmU"
        );
    			return new Cloudinary(config);
    }
}
