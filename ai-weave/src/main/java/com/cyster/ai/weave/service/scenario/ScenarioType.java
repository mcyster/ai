package com.cyster.ai.weave.service.scenario;

public interface ScenarioType {

    String name();

    String description();

    Class<?> parameterClass();

    Class<?> contextClass();

}