package com.entiros.starlify.connector.api.dto.mashery;

import lombok.Data;

import java.util.List;

@Data
public class MasheryResponse<T> {
    private int total;
    private int offset;
    private int limit;
    private String sort;
    private Object query;
    private List<T> collection;
}
