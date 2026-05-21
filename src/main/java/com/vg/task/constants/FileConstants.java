package com.vg.task.constants;

import java.util.Set;

public final class FileConstants {
    
    private FileConstants() {}
    
    public static final long MAX_FILE_SIZE_MB = 10;
    public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    public static final int MAX_FILES_PER_TASK = 5;
    
    public static final Set<String> ALLOWED_FILE_TYPES = Set.of(
        "pdf", "doc", "docx", "jpg", "jpeg", "png", "gif", "mp4", "txt", "xls", "xlsx", "zip"
    );
    
    public static boolean isAllowedFileType(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) return false;
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_FILE_TYPES.contains(extension);
    }
}
