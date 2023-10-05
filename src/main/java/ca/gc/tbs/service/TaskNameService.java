package ca.gc.tbs.service;

import ca.gc.tbs.domain.TaskName;
import ca.gc.tbs.repository.TaskNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskNameService {

    @Autowired
    private TaskNameRepository taskNameRepository;

    public List<TaskName> getAllTaskNames() {
        return taskNameRepository.findAll();
    }

}
