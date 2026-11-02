package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.dto.file.UploadFileVO;
import com.tesim.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /** 工单等场景：登录用户上传图片，返回可访问 URL */
    @PostMapping("/upload")
    public ApiResponse<UploadFileVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "scene", required = false, defaultValue = "tickets") String scene) {
        return ApiResponse.ok(fileUploadService.saveImage(file, scene));
    }
}
