package com.lovedev.api.service;

import com.lovedev.api.exception.FileStorageException;
import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * Service for file storage operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileUtil fileUtil;

    /**
     * Store avatar file
     */
    public String storeAvatar(MultipartFile file) {
        log.info("Storing avatar file: {}", file.getOriginalFilename());
        return fileUtil.storeFile(file, "avatars");
    }

    /**
     * Store document file
     */
    public String storeDocument(MultipartFile file) {
        log.info("Storing document file: {}", file.getOriginalFilename());
        return fileUtil.storeFile(file, "documents");
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileUtil.getFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }

    /**
     * Delete file
     */
    public void deleteFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            boolean deleted = fileUtil.deleteFile(fileName);
            if (!deleted) {
                log.warn("Failed to delete file: {}", fileName);
            }
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String fileName) {
        return fileUtil.fileExists(fileName);
    }

    /**
     * Get file URL
     */
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        // This should match your server configuration
        return "/api/v1/files/" + fileName;
    }

    /**
     * Get storage statistics
     */
    public StorageStats getStorageStats() {
        return StorageStats.builder()
                .totalSize(fileUtil.getTotalStorageSize())
                .totalSizeReadable(fileUtil.getReadableFileSize(fileUtil.getTotalStorageSize()))
                .fileCount(fileUtil.getFileCount())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class StorageStats {
        private long totalSize;
        private String totalSizeReadable;
        private long fileCount;
    }
}