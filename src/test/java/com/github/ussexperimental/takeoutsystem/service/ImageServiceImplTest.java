package com.github.ussexperimental.takeoutsystem.service;

import com.github.ussexperimental.takeoutsystem.service.ImageService;
import com.github.ussexperimental.takeoutsystem.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ImageServiceImplTest {

    private ImageServiceImpl imageService;

    @BeforeEach
    public void setUp() throws Exception {
        imageService = new ImageServiceImpl();

        // 使用反射设置 @Value 注解的字段
        setField(imageService, "uploadDir", "/mock/upload/dir");
        setField(imageService, "serverUrl", "http://mockserver.com");
    }

    /**
     * 使用反射设置私有字段的值
     *
     * @param target    目标对象
     * @param fieldName 字段名称
     * @param value     字段值
     * @throws Exception
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = ImageServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // 1. 测试 uploadImage 方法

    @Test
    @DisplayName("测试上传图片 - 成功")
    public void testUploadImage_Success() throws IOException {
        // 准备数据
        String originalFilename = "test.jpg";
        byte[] content = "dummy content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", originalFilename, "image/jpeg", content);

        // 使用 MockedStatic 模拟 Files 类的静态方法
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // 模拟 createDirectories
            Path mockPath = Paths.get("/mock/upload/dir");
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(mockPath);

            // 模拟 copy
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn((long) content.length);

            // 调用方法
            String imageUrl = imageService.uploadImage(multipartFile);

            // 验证
            assertNotNull(imageUrl);
            assertTrue(imageUrl.startsWith("http://mockserver.com/images/"));
            String fileName = imageUrl.substring("http://mockserver.com/images/".length());
            assertTrue(fileName.endsWith(".jpg"));
            assertEquals(36 + 4, fileName.length()); // UUID (36) + .jpg (4)

            // 验证静态方法调用
            mockedFiles.verify(() -> Files.createDirectories(Paths.get("/mock/upload/dir")), times(1));
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(Paths.get("/mock/upload/dir", fileName)), eq(StandardCopyOption.REPLACE_EXISTING)), times(1));
        }
    }

    @Test
    @DisplayName("测试上传图片 - 文件为空")
    public void testUploadImage_FileEmpty() {
        // 准备数据
        MultipartFile multipartFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageService.uploadImage(multipartFile);
        });

        assertEquals("文件为空", exception.getMessage());

        // 由于文件为空，Files 方法不应被调用，因此无需验证静态方法
    }

    @Test
    @DisplayName("测试上传图片 - 文件名为空")
    public void testUploadImage_FileNameNull() {
        // 准备数据
        byte[] content = "dummy content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", null, "image/jpeg", content);

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageService.uploadImage(multipartFile);
        });

        assertEquals("文件名称为空", exception.getMessage());

        // 由于文件名为空，Files 方法不应被调用，因此无需验证静态方法
    }

    // 2. 测试 deleteImage 方法

    @Test
    @DisplayName("测试删除图片 - 成功")
    public void testDeleteImage_Success() throws Exception {
        // 准备数据
        String imageUrl = "http://mockserver.com/images/test-image.jpg";
        String fileName = "test-image.jpg";
        Path filePath = Paths.get("/mock/upload/dir", fileName);

        // 使用 MockedStatic 模拟 Files 类的静态方法
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // 模拟 deleteIfExists
            mockedFiles.when(() -> Files.deleteIfExists(eq(filePath))).thenReturn(true);

            // 调用方法
            imageService.deleteImage(imageUrl);

            // 验证
            mockedFiles.verify(() -> Files.deleteIfExists(eq(filePath)), times(1));
        }
    }

    @Test
    @DisplayName("测试删除图片 - imageUrl 为 null")
    public void testDeleteImage_ImageUrlNull() throws IOException {
        // 调用方法
        imageService.deleteImage(null);

        // 无需验证 Files 方法调用，因为方法应直接返回
    }

    @Test
    @DisplayName("测试删除图片 - imageUrl 为空")
    public void testDeleteImage_ImageUrlEmpty() throws IOException {
        // 调用方法
        imageService.deleteImage("");

        // 无需验证 Files 方法调用，因为方法应直接返回
    }

    @Test
    @DisplayName("测试删除图片 - 文件不存在")
    public void testDeleteImage_FileNotExists() throws Exception {
        // 准备数据
        String imageUrl = "http://mockserver.com/images/nonexistent.jpg";
        String fileName = "nonexistent.jpg";
        Path filePath = Paths.get("/mock/upload/dir", fileName);

        // 使用 MockedStatic 模拟 Files 类的静态方法
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // 模拟 deleteIfExists 返回 false
            mockedFiles.when(() -> Files.deleteIfExists(eq(filePath))).thenReturn(false);

            // 调用方法
            imageService.deleteImage(imageUrl);

            // 验证
            mockedFiles.verify(() -> Files.deleteIfExists(eq(filePath)), times(1));
        }
    }

    @Test
    @DisplayName("测试删除图片 - imageUrl 格式错误")
    public void testDeleteImage_InvalidImageUrl() {
        // 准备数据
        String imageUrl = "invalid_url";

        // 调用方法并断言异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageService.deleteImage(imageUrl);
        });

        assertTrue(exception.getMessage().contains("无效的图片URL"));
    }
}
