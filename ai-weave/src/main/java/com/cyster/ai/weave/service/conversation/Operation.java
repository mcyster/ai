package com.cyster.ai.weave.service.conversation;

import java.util.List;
import java.util.Optional;

public interface Operation {
    String getDescription();
    List<Operation> children();
    Optional<Object> context(); 
}
