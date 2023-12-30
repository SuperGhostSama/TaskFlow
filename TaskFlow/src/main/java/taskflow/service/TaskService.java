package taskflow.service;

import taskflow.dto.request.TaskRequestDTO;
import taskflow.dto.response.TaskResponseDTO;
import taskflow.entities.User;
import taskflow.entities.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO);

    TaskResponseDTO getTask(Long id);

    List<TaskResponseDTO> getAllTasks();

    TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO);

    void deleteTask(Long id);

    List<TaskResponseDTO> getTasksByUser(User user);

    List<TaskResponseDTO> getTasksByStatus(TaskStatus status);

}
