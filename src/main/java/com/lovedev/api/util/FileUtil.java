package com.lovedev.api.util;

import com.lovedev.api.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for file operations
 */
@Component
@Slf4j
public class FileUtil {

    private final Path fileStorageLocation;
    private final List<String> allowedExtensions;
    private final long maxFileSize;

    public FileUtil(
            @Value("${app.file.upload-dir:./uploads}") String uploadDir,
            @Value("${app.file.allowed-extensions}") List<String> allowedExtensions,
            @Value("${app.file.max-size:5242880}") long maxFileSize) {

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = allowedExtensions;
        this.maxFileSize = maxFileSize;

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location created/verified: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store file with generated unique name
     */
    public String storeFile(MultipartFile file) {
        return storeFile(file, null);
    }

    /**
     * Store file with custom subdirectory
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        // Validate file
        validateFile(file);

        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if filename contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFileName);
            String newFileName = generateUniqueFileName(fileExtension);

            // Determine target location
            Path targetLocation;
            if (subDirectory != null && !subDirectory.isEmpty()) {
                Path subDirPath = this.fileStorageLocation.resolve(subDirectory);
                Files.createDirectories(subDirPath);
                targetLocation = subDirPath.resolve(newFileName);
            } else {
                targetLocation = this.fileStorageLocation.resolve(newFileName);
            }

            // Copy file to target location
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File stored successfully: {}", newFileName);

            // Return relative path
            return subDirectory != null
                    ? subDirectory + "/" + newFileName
                    : newFileName;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully: {}", fileName);
            return true;
        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileName, ex);
            return false;
        }
    }

    /**
     * Get file path
     */
    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            return Files.exists(filePath);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Validate file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum limit of %d bytes", maxFileSize)
            );
        }

        // Check file extension
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);

        if (!isAllowedExtension(extension)) {
            throw new FileStorageException(
                    String.format("File type .%s is not allowed. Allowed types: %s",
                            extension, allowedExtensions)
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new FileStorageException("Invalid file content type: " + contentType);
        }
    }

    /**
     * Get file extension
     */
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Check if extension is allowed
     */
    private boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * Check if content type is allowed
     */
    private boolean isAllowedContentType(String contentType) {
        List<String> allowedContentTypes = Arrays.asList(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/webp"
        );
        return allowedContentTypes.contains(contentType.toLowerCase());
    }

    /**
     * Generate unique filename
     */
    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * Get file size in readable format
     */
    public String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format("%.2f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    /**
     * Clean up old files (files older than specified days)
     */
    public void cleanupOldFiles(int daysOld) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);

            Files.walk(fileStorageLocation)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old file: {}", path.getFileName());
                        } catch (IOException e) {
                            log.error("Could not delete old file: {}", path.getFileName(), e);
                        }
                    });

            log.info("Cleanup completed for files older than {} days", daysOld);
        } catch (IOException ex) {
            log.error("Error during file cleanup", ex);
        }
    }

    /**
     * Get total storage size
     */
    public long getTotalStorageSize() {
        try {
            return Files.walk(fileStorageLocation)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException ex) {
            log.error("Error calculating storage size", ex);
            return 0L;
        }
    }

    /**
     * Get file count
     */
    public long getFileCount() {
        try {
            return Files.walk(fileStorageLocation)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException ex) {
            log.error("Error counting files", ex);
            return 0L;
        }
    }
}