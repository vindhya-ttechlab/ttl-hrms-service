package com.ttl.userportal.service.storage;

import com.ttl.userportal.service.storage.model.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ReimbursementFileStorageService {

    @Value("${reimbursement.upload.dir}")
    private String uploadDir;

    public StoredFile storeFile(Long reimbursementId, MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(uploadDir, reimbursementId.toString());
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename, file.getContentType());

        String storedName = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(storedName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return new StoredFile(originalFilename != null ? originalFilename : storedName, storedName, filePath.toString(), file.getSize(), file.getContentType());
    }

    private String extractExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return getExtensionFromContentType(contentType);
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return "";

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

}
