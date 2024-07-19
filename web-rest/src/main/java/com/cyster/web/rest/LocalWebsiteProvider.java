package com.cyster.web.rest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cyster.web.weave.scenarios.WebsiteProvider;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website.Type;

import java.nio.file.attribute.FileTime;
import java.util.Comparator;

public class LocalWebsiteProvider implements WebsiteProvider {
    private URI baseUri;
    private Path baseDirectory;

    public LocalWebsiteProvider(URI baseUri, Path baseDirectory) {
        this.baseUri = baseUri;
        this.baseDirectory = baseDirectory;
    }

    public List<Website> getSites() {
        List<Website> sites = new ArrayList<>();

        for(Type type: Website.Type.values()) {
            Path typeRoot = baseDirectory.resolve(type.toString().toLowerCase());
            if (!Files.exists(typeRoot)) {
                continue;
            }

            try (Stream<Path> paths = Files.walk(typeRoot, 1)) {
                Function<Path, FileTime> getLastModifiedTime = path -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };

                var typedSites = paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(typeRoot))
                    .sorted(Comparator.comparing(getLastModifiedTime).reversed())
                    .map(path -> new WebsiteImpl(this.baseUri, this.baseDirectory, path.getFileName().toString(), type))
                    .collect(Collectors.toList());
                sites.addAll(typedSites);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return sites;
    }

    public Website getSite(String name) {
        Type websiteType = null;
        for(var type: Website.Type.values()) {
            var websiteRoot = baseDirectory.resolve(type.toString().toLowerCase()).resolve(name);
            if (Files.exists(websiteRoot)) {
                websiteType = type;
                break;
            }
        }
        if (websiteType == null) {
            throw new RuntimeException("Wesbite not found: " + name);
        }

        return new WebsiteImpl(this.baseUri, baseDirectory, name, websiteType);
    }

    @Override
    public Website create() {
        String name = UUID.randomUUID().toString();
        return create(Type.Unmanaged, name);
    }

    @Override
    public Website copy(Website website) {
        var newWebsite = create();

        clone(website, newWebsite);

        return newWebsite;
    }

    private Website create(Type type, String name) {
        Path directory = baseDirectory.resolve(type.toString().toLowerCase()).resolve(name);

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create site directory: " + directory.toString());
        }

        return new WebsiteImpl(baseUri, baseDirectory, name, type);
    }

    private Website clone(Website fromWebsite, Website toWebsite) {
        for(var assetName: fromWebsite.getAssets()) {
            toWebsite.putAsset(assetName, fromWebsite.getAsset(assetName).content());
        }
        return toWebsite;
    }

}
