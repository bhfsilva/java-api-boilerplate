package br.com.boilerplate.utils;

import br.com.boilerplate.dtos.filename.Filename;

import java.io.File;

public class FilenameHelper {
    public static Filename getFilenameData(String filename) {
        var file = new File(filename);
        var nameWithExtension = file.getName();
        var firstDotIndex = nameWithExtension.indexOf('.');

        var nameWithoutExtension = (firstDotIndex != -1) ? nameWithExtension.substring(0, firstDotIndex) : nameWithExtension;
        var extension = nameWithoutExtension.equals(nameWithExtension) ? "" : nameWithExtension.substring(firstDotIndex);

        return new Filename(file.getParent(), nameWithoutExtension, extension, nameWithExtension);
    }
}
