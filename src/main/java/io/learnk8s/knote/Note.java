package io.learnk8s.knote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notes")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

class Note {
    @Id
    private String id;
    private String description;

    @Override
    public String toString() {
        return description;
    }
}