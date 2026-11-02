package com.tesim.service.impl;

import com.tesim.common.BizException;
import com.tesim.config.UploadProperties;
import com.tesim.dto.file.UploadFileVO;
import com.tesim.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final UploadProperties uploadProperties;

    @Override
    public UploadFileVO saveImage(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择图片");
        }
        if (file.getSize() > uploadProperties.getMaxBytes()) {
            throw new BizException("图片不能超过 5MB");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED.contains(contentType)) {
            throw new BizException("仅支持 JPG / PNG / GIF / WEBP");
        }

        String ext = extensionOf(file.getOriginalFilename(), contentType);
        String folder = StringUtils.hasText(subDir) ? subDir.trim().replaceAll("[^a-zA-Z0-9_-]", "") : "misc";
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path base = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();
        Path targetDir = base.resolve(folder);
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BizException("上传失败，请稍后重试");
        }

        String url = "/api" + uploadProperties.getPublicPath() + "/" + folder + "/" + filename;
        return UploadFileVO.builder()
                .url(url)
                .name(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }

    private static String extensionOf(String original, String contentType) {
        if (StringUtils.hasText(original) && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.(jpe?g|png|gif|webp)")) {
                return ext.equals(".jpeg") ? ".jpg" : ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
