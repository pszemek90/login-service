package com.prezcode.loginservice.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.OptBoolean;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, requireTypeIdForSubtypes = OptBoolean.FALSE)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TokenResponse.class, name = "TokenResponse")
})
public interface LoginResponse {
}
