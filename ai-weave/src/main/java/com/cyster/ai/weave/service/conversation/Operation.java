package com.cyster.ai.weave.service.conversation;

import java.util.List;
import java.util.Optional;

public interface Operation {
    enum Level {
        Debug,
        Verbose,
        Normal
    }

    Level getLevel();
    String getDescription();
    List<Operation> children();
    Optional<Object> context();
}
