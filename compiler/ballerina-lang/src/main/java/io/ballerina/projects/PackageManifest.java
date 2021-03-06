/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a Ballerina.toml file.
 *
 * @since 2.0.0
 */
public class PackageManifest {
    private final PackageDescriptor packageDesc;
    private final List<Dependency> dependencies;
    private final Map<String, Platform> platforms;
    private final DiagnosticResult diagnostics;
    private final List<String> license;
    private final List<String> authors;
    private final List<String> keywords;
    private final String repository;
    private final List<String> exported;

    // Other entries hold other key/value pairs available in the Ballerina.toml file.
    // These keys are not part of the Ballerina package specification.
    private final Map<String, TopLevelNode> otherEntries;

    private PackageManifest(PackageDescriptor packageDesc,
                            List<Dependency> dependencies,
                            Map<String, Platform> platforms,
                            Map<String, TopLevelNode> otherEntries,
                            DiagnosticResult diagnostics) {
        this.packageDesc = packageDesc;
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.platforms = Collections.unmodifiableMap(platforms);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = Collections.emptyList();
        this.authors = Collections.emptyList();
        this.keywords = Collections.emptyList();
        this.exported = Collections.emptyList();
        this.repository = "";
    }

    private PackageManifest(PackageDescriptor packageDesc,
                            List<Dependency> dependencies,
                            Map<String, Platform> platforms,
                            Map<String, TopLevelNode> otherEntries,
                            DiagnosticResult diagnostics,
                            List<String> license,
                            List<String> authors,
                            List<String> keywords,
                            List<String> exported,
                            String repository) {
        this.packageDesc = packageDesc;
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.platforms = Collections.unmodifiableMap(platforms);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = license;
        this.authors = authors;
        this.keywords = keywords;
        this.exported = exported;
        this.repository = repository;
    }

    public static PackageManifest from(PackageDescriptor packageDesc) {
        return new PackageManifest(packageDesc, Collections.emptyList(),
                Collections.emptyMap(), Collections.emptyMap(), new DefaultDiagnosticResult(Collections.EMPTY_LIST));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       List<Dependency> dependencies,
                                       Map<String, Platform> platforms) {
        return new PackageManifest(packageDesc, dependencies, platforms, Collections.emptyMap(),
                new DefaultDiagnosticResult(Collections.EMPTY_LIST));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       List<Dependency> dependencies,
                                       Map<String, Platform> platforms,
                                       Map<String, TopLevelNode> otherEntries,
                                       DiagnosticResult diagnostics,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> exported,
                                       String repository) {
        return new PackageManifest(packageDesc, dependencies, platforms, otherEntries, diagnostics, license, authors,
                                   keywords, exported, repository);
    }

    public PackageName name() {
        return packageDesc.name();
    }

    public PackageOrg org() {
        return packageDesc.org();
    }

    public PackageVersion version() {
        return packageDesc.version();
    }

    public PackageDescriptor descriptor() {
        return packageDesc;
    }

    public List<Dependency> dependencies() {
        return dependencies;
    }

    public Platform platform(String platformCode) {
        return platforms.get(platformCode);
    }

    // TODO Do we need to custom key/value par mapping here
    public TopLevelNode getValue(String key) {
        return otherEntries.get(key);
    }

    public List<String> license() {
        return license;
    }

    public List<String> authors() {
        return authors;
    }

    public List<String> keywords() {
        return keywords;
    }

    public List<String> exported() {
        return exported;
    }

    public String repository() {
        return repository;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    /**
     * Represents a dependency of a package.
     *
     * @since 2.0.0
     */
    public static class Dependency {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion semanticVersion;

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }

        public PackageVersion version() {
            return semanticVersion;
        }
    }

    /**
     * Represents the platform section in Ballerina.toml file.
     *
     * @since 2.0.0
     */
    public static class Platform {
        // We could eventually add more things to the platform
        private final List<Map<String, Object>> dependencies;
        private final List<Map<String, Object>> repositories;

        public Platform(List<Map<String, Object>> dependencies) {
            this(dependencies, Collections.emptyList());
        }

        public Platform(List<Map<String, Object>> dependencies, List<Map<String, Object>> repositories) {
            if (dependencies != null) {
                this.dependencies = Collections.unmodifiableList(dependencies);
            } else {
                this.dependencies = Collections.emptyList();
            }
            if (repositories != null) {
                this.repositories = Collections.unmodifiableList(repositories);
            } else {
                this.repositories = Collections.emptyList();
            }
        }

        public List<Map<String, Object>> dependencies() {
            return dependencies;
        }

        public List<Map<String, Object>> repositories() {
            return repositories;
        }
    }
}
