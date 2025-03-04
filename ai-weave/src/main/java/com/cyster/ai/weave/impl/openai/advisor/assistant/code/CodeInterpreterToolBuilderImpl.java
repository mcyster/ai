package com.cyster.ai.weave.impl.openai.advisor.assistant.code;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.tool.CodeInterpreterTool;
import com.cyster.ai.weave.service.tool.CodeInterpreterTool.Asset;

import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;

public class CodeInterpreterToolBuilderImpl<CONTEXT> implements CodeInterpreterTool.Builder<CONTEXT> {
    private final OpenAiService openAiService;
    private final Class<CONTEXT> contextClass;
    private final Consumer<CodeInterpreterToolImpl<CONTEXT>> codeInterpreterToolResult;
    private final List<Asset> assets = new ArrayList<>();
    private String name;

    public CodeInterpreterToolBuilderImpl(OpenAiService openAiService, Class<CONTEXT> contextClass,
            Consumer<CodeInterpreterToolImpl<CONTEXT>> codeInterpreterToolResult) {
        this.openAiService = openAiService;
        this.contextClass = contextClass;
        this.codeInterpreterToolResult = codeInterpreterToolResult;
    }

    @Override
    public CodeInterpreterToolBuilderImpl<CONTEXT> addAsset(String name, String contents) {
        this.assets.add(new StringAsset(name, contents));
        return this;
    }

    @Override
    public CodeInterpreterToolBuilderImpl<CONTEXT> addAsset(Asset asset) {
        this.assets.add(asset);
        return this;
    }

    @Override
    public CodeInterpreterTool<CONTEXT> create() {
        List<String> fileIds = new ArrayList<String>();

        try {
            var directory = Files.createTempDirectory("store-" + safeName(this.name));

            for (var document : this.assets) {
                var name = document.getName();
                var extension = ".txt";

                int lastDotIndex = name.lastIndexOf('.');
                if (lastDotIndex != -1) {
                    extension = name.substring(lastDotIndex + 1);
                    name = name.substring(0, lastDotIndex);
                }

                var safeName = safeName(name);
                var safeExtension = "." + safeName(extension);

                Path realFile = Paths.get(directory.toString(), safeName + safeExtension);

                Files.createFile(realFile);
                try (InputStream inputStream = document.getInputStream()) {
                    Files.copy(inputStream, realFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    var fileUpload = new UploadFileRequest(realFile, "assistants");
                    var file = this.openAiService.createClient(FilesClient.class).uploadFile(fileUpload);
                    fileIds.add(file.id());
                }
                Files.delete(realFile);
            }

            if (directory != null) {
                Files.delete(directory);
            }
        } catch (IOException e) {
            // TODO better error
            throw new RuntimeException(e);
        }

        var tool = new CodeInterpreterToolImpl<CONTEXT>(fileIds, contextClass);
        codeInterpreterToolResult.accept(tool);

        return tool;
    }

    private static String safeName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }

    private class StringAsset implements Asset {
        private String name;
        private String contents;

        public StringAsset(String name, String contents) {
            this.name = name;
            this.contents = contents;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contents.getBytes());
        }
    }

}
