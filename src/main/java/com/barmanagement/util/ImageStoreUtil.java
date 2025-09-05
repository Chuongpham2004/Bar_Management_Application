package com.barmanagement.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Locale;

public class ImageStoreUtil {

    /** Thư mục ảnh của app nằm ngoài JAR để ghi được: ~/.barapp/images/menu */
    public static Path getAppMenuImageDir() throws IOException {
        Path dir = Paths.get(System.getProperty("user.home"), ".barapp", "images", "menu");
        Files.createDirectories(dir);
        return dir;
    }

    /** Slug hoá tên file + giữ đuôi */
    public static String slugifyFileName(String baseName) {
        String name = baseName;
        int dot = baseName.lastIndexOf('.');
        String ext = dot >= 0 ? baseName.substring(dot) : "";
        name = dot >= 0 ? baseName.substring(0, dot) : baseName;

        String norm = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        norm = norm.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (norm.isEmpty()) norm = "image";
        return norm + ext.toLowerCase(Locale.ROOT);
    }

    /** Copy file ảnh vào thư mục app; nếu trùng tên sẽ thêm số đếm */
    public static Path copyToAppImages(File source) throws IOException {
        Path targetDir = getAppMenuImageDir();
        String fileName = slugifyFileName(source.getName());
        Path target = targetDir.resolve(fileName);

        int i = 1;
        while (Files.exists(target)) {
            String base = fileName;
            String ext = "";
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0) {
                base = fileName.substring(0, dot);
                ext = fileName.substring(dot);
            }
            target = targetDir.resolve(base + "_" + i + ext);
            i++;
        }
        return Files.copy(source.toPath(), target, StandardCopyOption.COPY_ATTRIBUTES);
    }
}
