package ca.gc.tbs.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "badwords")
public class BadWordEntry {

    @Id
    private String id;

    @Indexed
    private String word;

    private String language;

    @Indexed
    private String type;

    private boolean active = true;

    public BadWordEntry() {}

    public BadWordEntry(String word, String language, String type) {
        this.word = word.toLowerCase().trim();
        this.language = language;
        this.type = type;
        this.active = true;
    }

    public String getId() {return id;}
    public String getWord() {return word;}
    public String getLanguage() {return language;}
    public String getType() {return type;}
    public boolean isActive() {return active;}

    public void setId(String id) {this.id = id;}
    public void setWord(String word) {this.word = word.toLowerCase().trim();}
    public void setLanguage(String language) {this.language = language;}
    public void setType(String type) {this.type = type;}
    public void setActive(boolean active) {this.active = active;}
}

