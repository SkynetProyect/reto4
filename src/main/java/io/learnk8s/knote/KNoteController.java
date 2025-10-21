package io.learnk8s.knote;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;

@Controller
@EnableConfigurationProperties(KnoteProperties.class)
class KNoteController {

    @Autowired
    private NotesRepository notesRepository;
    @Autowired
    private KnoteProperties properties;

    private Parser parser = Parser.builder().build();
    private HtmlRenderer renderer = HtmlRenderer.builder().build();

    private MinioClient minioClient;

    @PostConstruct
    public void init() throws InterruptedException {
        initMinio();
    }

    @GetMapping("/")
    public String index(Model model) {
        getAllNotes(model);
        return "index";
    }

    @GetMapping(value = "/img/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImageByName(@PathVariable String name) throws Exception {
        InputStream imageStream = minioClient.getObject(properties.getMinioBucket(), name);
        return IOUtils.toByteArray(imageStream);
    }

    @PostMapping("/note")
    public String saveNotes(@RequestParam("image") MultipartFile file,
                            @RequestParam String description,
                            @RequestParam(required = false) String publish,
                            @RequestParam(required = false) String upload,
                            Model model) throws Exception {

        // Si se envía imagen, procesarla
        if (file != null && !file.isEmpty()) {
            uploadImage(file, description, model);
        }

        // Guardar la nota si hay contenido
        if ((publish != null && publish.equals("Publish")) || upload != null) {
            if (description != null && !description.trim().isEmpty()) {
                Node document = parser.parse(description.trim());
                String html = renderer.render(document);
                notesRepository.save(new Note(null, html));
            }
        }

        // Recuperar todas las notas para mostrar
        List<Note> notes = notesRepository.findAll();
        Collections.reverse(notes);
        model.addAttribute("notes", notes);

        // Limpiar textarea
        model.addAttribute("description", "");

        return "index";
    }

    private void uploadImage(MultipartFile file, String description, Model model) throws Exception {
        String fileId = UUID.randomUUID().toString() + "." + file.getOriginalFilename().split("\\.")[1];
        minioClient.putObject(properties.getMinioBucket(), fileId, file.getInputStream(),
                file.getSize(), null, null, file.getContentType());
        model.addAttribute("description",
                description + " ![](/img/" + fileId + ")");
    }

    private void getAllNotes(Model model) {
        List<Note> notes = notesRepository.findAll();
        Collections.reverse(notes);
        model.addAttribute("notes", notes);
    }


    private void initMinio() throws InterruptedException {
        boolean success = false;
        while (!success) {
            try {
                minioClient = new MinioClient("http://" + properties.getMinioHost() + ":9000" ,
                        properties.getMinioAccessKey(),
                        properties.getMinioSecretKey(),
                        false);
                // Check if the bucket already exists.
                boolean isExist = minioClient.bucketExists(properties.getMinioBucket());
                if (isExist) {
                    System.out.println("> Bucket already exists.");
                } else {
                    minioClient.makeBucket(properties.getMinioBucket());
                }
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("> Minio Reconnect: " + properties.isMinioReconnectEnabled());
                if (properties.isMinioReconnectEnabled()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    success = true;
                }
            }
        }
        System.out.println("> Minio initialized!");
    }


}

