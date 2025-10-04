package io.github.spencerpark.ijava.magics.dependencies;

import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class CommonRepositories {
    protected static final String MAVEN_PATTERN_PREFIX = "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier])";
    protected static final String MAVEN_ARTIFACT_PATTERN = MAVEN_PATTERN_PREFIX + ".[ext]";
    protected static final String MAVEN_POM_PATTERN = MAVEN_PATTERN_PREFIX + ".pom";

    public static DependencyResolver maven(String name, String urlRaw) {
        return maven(name, urlRaw, null, null);
    }

    public static DependencyResolver maven(String name, String urlRaw, String username, String password) {
        IBiblioResolver resolver = new IBiblioResolver();
        resolver.setM2compatible(true);
        resolver.setUseMavenMetadata(true);
        resolver.setUsepoms(true);

        // If authentication is provided, embed it in the URL
        String finalUrl = urlRaw;
        if (username != null && password != null) {
            try {
                URL url = new URL(urlRaw);
                // Reconstruct URL with authentication: https://username:password@host/path
                finalUrl = url.getProtocol() + "://" + username + ":" + password + "@" + 
                          url.getHost() + 
                          (url.getPort() != -1 ? ":" + url.getPort() : "") + 
                          url.getPath() +
                          (url.getQuery() != null ? "?" + url.getQuery() : "");
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid repository URL: " + urlRaw, e);
            }
        }

        resolver.setRoot(finalUrl);
        resolver.setName(name);

        return resolver;
    }

    public static DependencyResolver mavenCentral() {
        return CommonRepositories.maven("maven-central", "https://repo.maven.apache.org/maven2/");
    }

    public static DependencyResolver jcenter() {
        return CommonRepositories.maven("jcenter", "https://jcenter.bintray.com/");
    }

    public static DependencyResolver mavenLocal() {
        IBiblioResolver resolver = new IBiblioResolver();
        resolver.setM2compatible(true);
        resolver.setUseMavenMetadata(true);
        resolver.setUsepoms(true);

        resolver.setName("maven-local");

        Path localRepoPath;
        try {
            localRepoPath = Maven.getInstance().getConfiguredLocalRepositoryPath();
        } catch (IOException e) {
            throw new RuntimeException("Error reading maven settings. " + e.getLocalizedMessage(), e);
        } catch (SAXException e) {
            throw new RuntimeException("Error parsing maven settings. " + e.getLocalizedMessage(), e);
        }

        resolver.setRoot("file:///" + localRepoPath.toString());

        return resolver;
    }
}
