#!/usr/bin/env python3
import os
import re
import sys
import pathlib
import argparse

def normalize_string(string):
    if not re.match(r'^[A-Za-z _-]+$', string):
        raise ValueError(f"Invalid characters in input: {string}")

    transformed_string = re.sub(r'[_-]+', " ", string)

    # add space before capital letters
    transformed_string = re.sub(r'([a-z])([A-Z])', r'\1 \2', transformed_string)       # camelCase
    transformed_string = re.sub(r'([A-Z])([A-Z][a-z])', r'\1 \2', transformed_string)  # PascalCase
    transformed_string = re.sub(r'\s+', " ", transformed_string).strip()

    return transformed_string.lower()

# Parse arguments
parser = argparse.ArgumentParser(
    description="Helper script to update project text references and optionally generate Java entity classes."
)

# Replacement options
replace_group = parser.add_argument_group("Replacement options")
replace_group.add_argument(
    "--from", dest="source", default="default project", type=normalize_string,
    help="Text to search for in files (default: 'default project')."
)
replace_group.add_argument(
    "--to", dest="target", required=False, type=normalize_string,
    help="Replacement text for the matched source string."
)
replace_group.add_argument(
    "--dir", dest="root_dir", default=".",
    help="Root directory to process (default: current working directory)."
)

# Code generation options
gen_group = parser.add_argument_group("Code generation")
gen_group.add_argument(
    "--add", dest="add_class", required=False, type=normalize_string,
    help="Generate Java entity-related classes with the specified name."
)
args = parser.parse_args()

# Helpers to transform words
def split_string_in_words(string):
    """Split input into words: 'foo bar project' -> ['foo', 'bar', 'project']"""
    return re.split(r"[\s]+", string.strip())

def build_variants(string):
    """Return dict of style variants from input string"""
    words = split_string_in_words(string)

    last_word = words[-1]
    if last_word.endswith("y"):
        plural_prefix = last_word[:-1] + "ies"
    else:
        plural_prefix = last_word + "s"

    return {
        "lower": "".join(w.lower() for w in words),                             # foobarproject
        "pascal": "".join(w.capitalize() for w in words),                       # FooBarProject
        "camel": words[0].lower() + "".join(w.capitalize() for w in words[1:]), # fooBarProject
        "kebab": "-".join(w.lower() for w in words),                            # foo-bar-project
        "space": " ".join(w.capitalize() for w in words),                       # Foo Bar Project
        "snake": "_".join(w.lower() for w in words),                            # foo_bar_project
        "snake-plural": "_".join(w.lower() for w in words[:-1] + [plural_prefix]),# foo_bar_projects
        "kebab-plural": "-".join(w.lower() for w in words[:-1] + [plural_prefix]) # foo_bar_projects
    }

def replace_all(text):
    """Apply replacements style-by-style"""
    for style, regex in regexes.items():
        text = regex.sub(mapping[style], text)
    return text

def file_count(path):
    return len([item for item in pathlib.Path(path).iterdir() if item.is_file()])

def is_hidden_file(path):
    return any(part.startswith(".") for part in path.parts)

root_dir = pathlib.Path(args.root_dir)
main_package = pathlib.Path(root_dir) / "src" / "main"
resources_package = f"{main_package}/resources"
project_name = None

with open(f"{resources_package}/application.yaml", "r") as f:
    for line in f:
        line = line.strip()
        if line.startswith("name:"):
            project_name = line.split(":", 1)[1].strip()
            break

project_name_words = re.sub(r'(?<!^)(?=[A-Z])', ' ', project_name)
project_name = project_name_words.lower()

project_name_variants = build_variants(project_name)

def build_java_file(template_type):
    """Create a Java file based on template_type ('entities', 'repositories', etc.)."""
    class_name = args.add_class.strip()
    class_name_variants = build_variants(class_name)
    project_name = project_name_variants["lower"]

    project_package = pathlib.Path("br") / "com" / project_name
    project_package_complete_path = main_package / "java" / project_package
    project_package_parts = '.'.join(project_package.parts)

    if not os.path.exists(project_package_complete_path):
        project_name = project_name_variants["lower"] + "project"

        project_package = pathlib.Path("br") / "com" / project_name
        project_package_complete_path = main_package / "java" / project_package
        project_package_parts = '.'.join(project_package.parts)

    templates = {
        "entity": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": "entities",
            "filename": f"{class_name_variants['pascal']}.java",
            "content": f"""package {project_package_parts}.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "{class_name_variants['snake-plural']}")
public class {class_name_variants['pascal']} {{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
}}
"""
        },
        "repository": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": f"repositories/{class_name_variants['lower']}",
            "filename": f"{class_name_variants['pascal']}JpaRepository.java",
            "content": f"""package {project_package_parts}.repositories.{class_name_variants['lower']};

import {project_package_parts}.entities.{class_name_variants['pascal']};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface {class_name_variants['pascal']}JpaRepository extends JpaRepository<{class_name_variants['pascal']}, UUID> {{
}}
"""
        },
        "repositoryImpl": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": f"repositories/{class_name_variants['lower']}",
            "filename": f"{class_name_variants['pascal']}RepositoryImpl.java",
            "content": f"""package {project_package_parts}.repositories.{class_name_variants['lower']};

import {project_package_parts}.entities.{class_name_variants['pascal']};
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class {class_name_variants['pascal']}RepositoryImpl {{
    private final {class_name_variants['pascal']}JpaRepository jpaRepository;
}}
"""
        },
        "mapper": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": f"mappers/{class_name_variants['lower']}",
            "filename": f"{class_name_variants['pascal']}StructMapper.java",
            "content": f"""package {project_package_parts}.mappers.{class_name_variants['lower']};

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface {class_name_variants['pascal']}StructMapper {{
}}
"""
        },
        "mapperImpl": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": f"mappers/{class_name_variants['lower']}",
            "filename": f"{class_name_variants['pascal']}MapperImpl.java",
            "content": f"""package {project_package_parts}.mappers.{class_name_variants['lower']};

import br.com.{project_name}.config.annotations.Mapper;
import lombok.RequiredArgsConstructor;

@Mapper
@RequiredArgsConstructor
public class {class_name_variants['pascal']}MapperImpl {{
    private final {class_name_variants['pascal']}StructMapper structMapper;
}}
"""
        },
        "specs": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": "rest/specs",
            "filename": f"{class_name_variants['pascal']}ControllerSpecs.java",
            "content": f"""package {project_package_parts}.rest.specs;

import br.com.{project_name}.rest.specs.commons.ApiResponseInternalServerError;
import io.swagger.v3.oas.annotations.tags.Tag;

@ApiResponseInternalServerError
@Tag(name = "{file_count(f"{project_package_complete_path}/rest/specs") + 1}. {class_name_variants['space']}", description = "{class_name_variants['space']} operations")
public interface {class_name_variants['pascal']}ControllerSpecs {{
}}
"""
        },
        "controller": {
            "root_package": pathlib.Path(project_package_complete_path),
            "package": "rest/controllers",
            "filename": f"{class_name_variants['pascal']}Controller.java",
            "content": f"""package {project_package_parts}.rest.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.{project_name}.rest.specs.{class_name_variants['pascal']}ControllerSpecs;

@RestController
@RequestMapping("/{class_name_variants["kebab-plural"]}")
@RequiredArgsConstructor
public class {class_name_variants['pascal']}Controller implements {class_name_variants['pascal']}ControllerSpecs {{
}}
"""
     },
     "migration": {
         "root_package": pathlib.Path(resources_package),
         "package": "db.migration",
         "filename": f"V{file_count(f"{resources_package}/db.migration") + 1}__create_{class_name_variants['snake-plural']}_table.sql",
         "content": f"""CREATE TABLE IF NOT EXISTS {class_name_variants['snake-plural']} (
    id UUID PRIMARY KEY
);"""
     },
     "dtos": {
         "root_package": pathlib.Path(project_package_complete_path),
         "package": f"dtos/{class_name_variants['lower']}",
         "filename": ".gitkeep",
         "content": ""
     },
     "usecases": {
         "root_package": pathlib.Path(project_package_complete_path),
         "package": f"usecases/{class_name_variants['lower']}",
         "filename": ".gitkeep",
         "content": ""
     }
}

    if template_type not in templates:
        raise ValueError(f"Unknown template type: {template_type}")

    class_template = templates[template_type]
    class_package = class_template["root_package"] / class_template["package"]
    class_package.mkdir(parents=True, exist_ok=True)

    complete_class_path = class_package / class_template["filename"]
    if complete_class_path.exists():
        print(f"File already exists: {complete_class_path}")
    else:
        complete_class_path.write_text(class_template["content"], encoding="utf-8")
        print(f"Created file: {complete_class_path}")

if args.add_class:
    build_java_file("entity")
    build_java_file("repository")
    build_java_file("repositoryImpl")
    build_java_file("mapper")
    build_java_file("mapperImpl")
    build_java_file("specs")
    build_java_file("controller")
    build_java_file("migration")
    build_java_file("dtos")
    build_java_file("usecases")

if args.target:
    from_parameter_value = split_string_in_words(args.source)
    if len(from_parameter_value) == 1:
        args.source = args.source.strip() + " project"

    to_parameter_value = split_string_in_words(args.target)
    if len(to_parameter_value) == 1:
        args.target = args.target.strip() + " project"

    # Build mappings
    source_variants = build_variants(args.source)
    target_variants = build_variants(args.target)

    mapping = {
        source_variants["lower"]: target_variants["lower"],
        source_variants["pascal"]: target_variants["pascal"],
        source_variants["camel"]: target_variants["camel"],
        source_variants["kebab"]: target_variants["kebab"],
        source_variants["space"]: target_variants["space"],
        source_variants["snake"]: target_variants["snake"]
    }

    # Compile individual regexes for exact matching
    regexes = {k: re.compile(re.escape(k)) for k in mapping.keys()}

    # Replace inside files
    for path in root_dir.rglob("*"):
        if path.is_file():
            if path.name == "build.py" or (is_hidden_file(path) and "azure" not in path.name):  # skip this script
                continue
            try:
                text = path.read_text(encoding="utf-8")
            except Exception:
                continue  # skip binary/unreadable files

            new_text = replace_all(text)
            if new_text != text:
                path.write_text(new_text, encoding="utf-8")
                print(f"Updated contents: {path}")

    # Rename files and directories
    for path in sorted(root_dir.rglob("*"), key=lambda p: len(p.parts), reverse=True):
        if path.name == "build.py" or (is_hidden_file(path) and "azure" not in path.name):  # skip this script
            continue

        new_name = replace_all(path.name)
        if new_name != path.name:
            new_path = path.with_name(new_name)
            path.rename(new_path)
            print(f"Renamed: {path} -> {new_path}")
