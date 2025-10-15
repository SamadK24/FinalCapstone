package com.aurionpro.service;

public interface CaptchaService {
    boolean verifyCaptcha(String captchaToken);
}
