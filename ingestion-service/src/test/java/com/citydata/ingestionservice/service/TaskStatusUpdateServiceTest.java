package com.citydata.ingestionservice.service;

import com.citydata.ingestionservice.constants.TaskStatus;
import com.citydata.ingestionservice.events.TaskStatusEvent;
import com.citydata.ingestionservice.model.Task;
import com.citydata.ingestionservice.model.Upload;
import com.citydata.ingestionservice.repository.TaskRepository;
import com.citydata.ingestionservice.repository.UploadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskStatusUpdateServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UploadRepository uploadRepository;

    @InjectMocks
    private TaskStatusUpdateService taskStatusUpdateService;

    @Test
    void updateTaskStatus_TaskFoundAndUpdated() {
        // Given
        String taskId = "task123";
        String uploadId = "upload456";

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setUploadId(uploadId);
        existingTask.setStatus(TaskStatus.PENDING);

        TaskStatusEvent event = new TaskStatusEvent(taskId, TaskStatus.COMPLETED, Instant.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        existingTask.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findByUploadId(uploadId)).thenReturn(List.of(existingTask));

        var existingUpload = new Upload();
        existingUpload.setId(uploadId);
        when(uploadRepository.findById(uploadId)).thenReturn(Optional.of(existingUpload));

        // When
        taskStatusUpdateService.updateTaskStatus(event);

        // Then
        verify(taskRepository).save(existingTask);
        verify(uploadRepository).findById(uploadId);
        verify(uploadRepository).save(any(Upload.class)); // Ensures file is marked as COMPLETED
    }

    @Test
    void updateTaskStatus_TaskNotFound() {
        // Given
        String taskId = "nonExistingTask";
        TaskStatusEvent event = new TaskStatusEvent(taskId, TaskStatus.COMPLETED, Instant.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        taskStatusUpdateService.updateTaskStatus(event);

        // Then
        verify(taskRepository, never()).save(any());
        verify(uploadRepository, never()).findById(any());
        verify(uploadRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_FileNotMarkedCompletedIfPendingTasksExist() {
        // Given
        String taskId = "task123";
        String uploadId = "upload456";

        Task completedTask = new Task();
        completedTask.setId(taskId);
        completedTask.setUploadId(uploadId);
        completedTask.setStatus(TaskStatus.COMPLETED);

        Task pendingTask = new Task();
        pendingTask.setId("task999");
        pendingTask.setUploadId(uploadId);
        pendingTask.setStatus(TaskStatus.PENDING);

        TaskStatusEvent event = new TaskStatusEvent(taskId, TaskStatus.COMPLETED, Instant.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(completedTask));
        when(taskRepository.findByUploadId(uploadId)).thenReturn(List.of(completedTask, pendingTask));

        // When
        taskStatusUpdateService.updateTaskStatus(event);

        // Then
        verify(taskRepository).save(completedTask);
        verify(uploadRepository, never()).save(any()); // Ensures file is NOT marked as COMPLETED
    }
}
