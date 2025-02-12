package com.citydata.ingestionservice.service;

import com.citydata.ingestionservice.constants.TaskStatus;
import com.citydata.ingestionservice.constants.UploadStatus;
import com.citydata.ingestionservice.events.TaskStatusEvent;
import com.citydata.ingestionservice.model.Task;
import com.citydata.ingestionservice.model.Upload;
import com.citydata.ingestionservice.repository.TaskRepository;
import com.citydata.ingestionservice.repository.UploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskStatusUpdateService {
    private final TaskRepository taskRepository;
    private final UploadRepository uploadRepository;

    @Transactional
    public void updateTaskStatus(TaskStatusEvent event) {
        Optional<Task> taskOpt = taskRepository.findById(event.getTaskId());

        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(event.getStatus());
            taskRepository.save(task);
            log.info("Updated task {} status to {}", event.getTaskId(), event.getStatus());

            // Check if all tasks for the file are completed
            if (allTasksCompletedForFile(task.getUploadId())) {
                markFileAsCompleted(task.getUploadId());
            }
        } else {
            log.warn("Task {} not found for status update", event.getTaskId());
        }
    }

    private boolean allTasksCompletedForFile(String uploadId) {
        if (uploadId == null) {
            return false; // No associated file
        }
        List<Task> tasks = taskRepository.findByUploadId(uploadId);
        return tasks.stream().allMatch(task -> TaskStatus.COMPLETED.equals(task.getStatus()));
    }

    private void markFileAsCompleted(String uploadId) {
        Optional<Upload> fileUploadOpt = uploadRepository.findById(uploadId);
        if (fileUploadOpt.isPresent()) {
            var fileUpload = fileUploadOpt.get();
            fileUpload.setStatus(UploadStatus.COMPLETED);
            uploadRepository.save(fileUpload);
            log.info("All tasks completed. Marking file {} as COMPLETED.", uploadId);
        }
    }
}
