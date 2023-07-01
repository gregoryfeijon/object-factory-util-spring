package br.com.gregoryfeijon.objectfactoryutilspring.model.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EnumBar {

    FIRST_ENUM(5, "First Enum"), SECOND_ENUM(10, "Second Enum");

    private final int id;
    private final String description;
}
