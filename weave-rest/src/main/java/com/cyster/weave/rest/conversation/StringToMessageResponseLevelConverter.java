package com.cyster.weave.rest.conversation;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMessageResponseLevelConverter implements Converter<String, MessageResponse.Level> {

    @Override
    public MessageResponse.Level convert(String source) {
        if (source == null || source.isEmpty()) {
            return MessageResponse.Level.quiet;
        }
        return MessageResponse.Level.valueOf(source.toLowerCase());
    }
}
