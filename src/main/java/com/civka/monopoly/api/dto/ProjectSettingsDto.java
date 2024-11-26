package com.civka.monopoly.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProjectSettingsDto {

    private ProjectType type;

    private Map<String, StatsDto> stats;
}
