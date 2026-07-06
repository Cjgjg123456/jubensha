package org.example.jubensha.entity;

import lombok.Data;

@Data
public class Act {
    private Integer actId;
    private Integer scriptId;
    private String actName;
    private Integer sort;
    private String publicContent;
}