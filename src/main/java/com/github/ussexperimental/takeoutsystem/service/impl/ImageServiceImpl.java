package com.github.ussexperimental.takeoutsystem.service.impl;

import com.github.ussexperimental.takeoutsystem.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${image.upload.dir}")
    private String uploadDir;

    @Value("${server.url}")
    private String serverUrl;

    // 上传图片
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名称为空");
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("文件扩展名缺失");
        }

        String extension = originalFilename.substring(lastDotIndex);
        String fileName = UUID.randomUUID().toString() + extension;

        Path path = Paths.get(uploadDir, fileName);
        Files.createDirectories(path.getParent());

        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return serverUrl + "/images/" + fileName;
    }

    // 删除图片
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        String fileName;
        try {
            fileName = Paths.get(new URL(imageUrl).getPath()).getFileName().toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("无效的图片URL", e);
        }

        Path path = Paths.get(uploadDir, fileName);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("删除图片失败", e);
        }
    }

}
