package com.aurionpro.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.aurionpro.service.CaptchaService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String SECRET_KEY = "6LfQs-YrAAAAAB_WE84MZT4bMnx_etGUZ_CoXY2n";

    @Override
    public boolean verifyCaptcha(String captchaToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = CAPTCHA_VERIFY_URL + "?secret=" + SECRET_KEY + "&response=" + captchaToken;

        Map<String, Object> response =
                restTemplate.postForObject(url, null, Map.class);

        if (response == null) {
            log.error("Captcha verification failed: response is null");
            return false;
        }

        return Boolean.TRUE.equals(response.get("success"));
    }
}

