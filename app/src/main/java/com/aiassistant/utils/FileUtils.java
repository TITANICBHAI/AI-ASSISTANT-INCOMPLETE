package com.aiassistant.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for file operations, including file reading/writing,
 * compression, and secure file management.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    
    /**
     * Read a text file into a string
     */
    public static String readTextFile(File file) {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + file.getAbsolutePath(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }
        
        return text.toString();
    }
    
    /**
     * Write a string to a text file
     */
    public static boolean writeTextFile(File file, String content) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file: " + file.getAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Read a binary file into a byte array
     */
    public static byte[] readBinaryFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        } catch (IOException e) {
            Log.e(TAG, "Error reading binary file: " + file.getAbsolutePath(), e);
            return null;
        }
    }
    
    /**
     * Write a byte array to a binary file
     */
    public static boolean writeBinaryFile(File file, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing binary file: " + file.getAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Copy a file from source to destination
     */
    public static boolean copyFile(File source, File destination) {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {
            
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            return false;
        }
    }
    
    /**
     * Create a zip file containing the given files
     */
    public static boolean createZipFile(File zipFile, List<File> filesToZip) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : filesToZip) {
                if (!file.exists()) {
                    continue;
                }
                
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                
                zos.closeEntry();
            }
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error creating zip file", e);
            return false;
        }
    }
    
    /**
     * Extract files from a zip file to the given directory
     */
    public static List<File> extractZipFile(File zipFile, File destDirectory) {
        List<File> extractedFiles = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            if (!destDirectory.exists()) {
                destDirectory.mkdirs();
            }
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDirectory, entry.getName());
                
                // Create parent directories if they don't exist
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                
                // Extract the file
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                
                extractedFiles.add(file);
                zis.closeEntry();
            }
            
            return extractedFiles;
        } catch (IOException e) {
            Log.e(TAG, "Error extracting zip file", e);
            return extractedFiles;
        }
    }
    
    /**
     * Get the SHA-256 hash of a file
     */
    public static String getFileSha256(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating file hash", e);
            return null;
        }
    }
    
    /**
     * Get the absolute path to the app's private storage directory
     */
    public static File getAppPrivateDir(Context context) {
        return context.getFilesDir();
    }
    
    /**
     * Get the absolute path to the app's private cache directory
     */
    public static File getAppCacheDir(Context context) {
        return context.getCacheDir();
    }
    
    /**
     * Get the absolute path to the app's external files directory
     */
    public static File getAppExternalFilesDir(Context context) {
        return context.getExternalFilesDir(null);
    }
    
    /**
     * Check if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    
    /**
     * Check if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || 
               Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
    
    /**
     * Delete a file or directory (recursively)
     */
    public static boolean deleteFileOrDirectory(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileOrDirectory(child);
                }
            }
        }
        
        return fileOrDir.delete();
    }
    
    /**
     * Create a temporary file in the app's cache directory
     */
    public static File createTempFile(Context context, String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix, context.getCacheDir());
        } catch (IOException e) {
            Log.e(TAG, "Error creating temp file", e);
            return null;
        }
    }
    
    /**
     * Copy a file from assets to the app's private storage
     */
    public static File copyAssetToFile(Context context, String assetName, String fileName) {
        try {
            File outputFile = new File(context.getFilesDir(), fileName);
            
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            
            return outputFile;
        } catch (IOException e) {
            Log.e(TAG, "Error copying asset to file", e);
            return null;
        }
    }
    
    /**
     * Get a list of files in a directory
     */
    public static List<File> listFiles(File directory) {
        List<File> files = new ArrayList<>();
        
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    files.add(file);
                }
            }
        }
        
        return files;
    }
    
    /**
     * Get a list of files in a directory matching a specific extension
     */
    public static List<File> listFilesWithExtension(File directory, String extension) {
        List<File> matchingFiles = new ArrayList<>();
        
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    if (file.isFile() && file.getName().endsWith(extension)) {
                        matchingFiles.add(file);
                    }
                }
            }
        }
        
        return matchingFiles;
    }
    
    /**
     * Get a list of subdirectories in a directory
     */
    public static List<File> listSubdirectories(File directory) {
        List<File> subdirectories = new ArrayList<>();
        
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    if (file.isDirectory()) {
                        subdirectories.add(file);
                    }
                }
            }
        }
        
        return subdirectories;
    }
    
    /**
     * Get the size of a file or directory (recursively) in bytes
     */
    public static long getSize(File fileOrDir) {
        if (fileOrDir.isFile()) {
            return fileOrDir.length();
        }
        
        long size = 0;
        if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += getSize(file);
                }
            }
        }
        
        return size;
    }
    
    /**
     * Format a file size in bytes to a human-readable string
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
