package com.aurionpro.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file) throws IOException;
}
