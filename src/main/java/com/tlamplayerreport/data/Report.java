package com.tlamplayerreport.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private int id;
    private UUID reporterUuid;
    private String reporterName;
    private ReportType type;
    private String category;
    private UUID targetUuid;
    private String targetName;
    private String description;
    private ReportStatus status;
    private long timestamp;
    private String reviewerName;
    private String notes;
}