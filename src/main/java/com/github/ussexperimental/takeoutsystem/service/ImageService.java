package com.github.ussexperimental.takeoutsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface ImageService {

    String uploadImage(MultipartFile file) throws IOException;

    void deleteImage(String imageUrl) throws IOException;
}
