package br.com.boilerplate.dtos.filename;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Filename {
    String parent;
    String name;
    String extension;
    String nameWithExtension;
}
