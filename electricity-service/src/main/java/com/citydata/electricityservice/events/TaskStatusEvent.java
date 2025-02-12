package com.citydata.electricityservice.events;

import com.citydata.electricityservice.constants.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatusEvent {
    private String taskId;
    private TaskStatus status;
    private Instant timestamp;
}

