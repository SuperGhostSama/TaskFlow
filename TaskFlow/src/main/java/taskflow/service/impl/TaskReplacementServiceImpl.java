package taskflow.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import taskflow.dto.request.TaskReplacementRequestDTO;
import taskflow.dto.response.TaskReplacementResponseDTO;
import taskflow.entities.Task;
import taskflow.entities.TaskReplacement;
import taskflow.entities.User;
import taskflow.entities.enums.TaskAction;
import taskflow.entities.enums.TaskReplacementStatus;
import taskflow.repository.TaskReplacementRepository;
import taskflow.repository.TaskRepository;
import taskflow.repository.UserRepository;
import taskflow.service.TaskReplacementService;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TaskReplacementServiceImpl implements TaskReplacementService {
    private final ModelMapper modelMapper;
    private final TaskReplacementRepository taskReplacementRepository;
    private final TaskRepository taskRepository;

    private final UserRepository userRepository;


    public TaskReplacementServiceImpl(ModelMapper modelMapper, TaskReplacementRepository taskReplacementRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.modelMapper = modelMapper;
        this.taskReplacementRepository = taskReplacementRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void deleteTaskReplacementById(Long id) {
        taskReplacementRepository.deleteById(id);
    }

    @Override
    public TaskReplacementResponseDTO createDeleteTaskReplacement(TaskReplacementRequestDTO requestDTO) {
        try {
            Long taskId = requestDTO.getTaskId();
            Long userId = requestDTO.getUserId();

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

            // Check if the provided userId matches the assignedToId of the task
            Long assignedToId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
            if (!Objects.equals(userId, assignedToId)) {
                throw new IllegalArgumentException("Invalid userId provided for Task with id: " + taskId);
            }

            // Store the assigned user before setting assignedTo to NULL
            User assignedToUser = task.getAssignedTo();

            // Set the assignedTo to NULL
            task.setAssignedTo(null);
            taskRepository.save(task);

            // Create TaskReplacement with DELETE action
            TaskReplacement taskReplacement = new TaskReplacement();
            taskReplacement.setTask(task);
            taskReplacement.setDateTime(LocalDateTime.now());

            // Set OldUser as the user to whom the task was assigned before the change
            taskReplacement.setOldUser(assignedToUser);

            taskReplacement.setNewUser(null); // Set to NULL for DELETE action
            taskReplacement.setAction(TaskAction.DELETE);
            taskReplacement.setStatus(TaskReplacementStatus.APPROVED);

            taskReplacement = taskReplacementRepository.save(taskReplacement);

            // Returning success message along with created task replacement details
            TaskReplacementResponseDTO responseDTO = modelMapper.map(taskReplacement, TaskReplacementResponseDTO.class);
            responseDTO.setStatus("success");
            responseDTO.setMessage("TaskReplacement created successfully");
            return responseDTO;
        } catch (EntityNotFoundException e) {
            return new TaskReplacementResponseDTO("error", "Task not found with id: " + requestDTO.getTaskId());
        } catch (IllegalArgumentException e) {
            return new TaskReplacementResponseDTO("error", e.getMessage());
        } catch (Exception e) {
            return new TaskReplacementResponseDTO("error", "Error creating task replacement: " + e.getMessage());
        }
    }

}
