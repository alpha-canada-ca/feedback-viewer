package ca.gc.tbs.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "taskNames")
public class TaskName {

    @Id
    private String id;

    private String name;

    public TaskName(String name) {
        this.name = name;
    }

    public TaskName() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
