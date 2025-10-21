package io.learnk8s.knote;
import org.springframework.data.mongodb.repository.MongoRepository;

interface NotesRepository extends MongoRepository<Note, String> {

}