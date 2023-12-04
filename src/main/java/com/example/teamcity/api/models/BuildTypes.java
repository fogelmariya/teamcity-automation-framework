package com.example.teamcity.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildTypes {
    private int count;
    private String href;
    private String nextHref;
    private String prevHref;
    private List<BuildType> buildType;
}
