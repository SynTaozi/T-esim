package com.tesim.service;

import com.tesim.dto.file.UploadFileVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    UploadFileVO saveImage(MultipartFile file, String subDir);
}
