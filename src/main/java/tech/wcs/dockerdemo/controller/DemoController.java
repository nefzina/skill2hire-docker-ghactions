package tech.wcs.dockerdemo.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.wcs.dockerdemo.persistence.entities.DemoEntity;
import tech.wcs.dockerdemo.persistence.repositories.DemoEntityRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
public class DemoController {

    private final DemoEntityRepository demoEntityRepository;
    private String releaseInfo;

    public DemoController(DemoEntityRepository demoEntityRepository) {
        this.demoEntityRepository = demoEntityRepository;
    }

    @PostConstruct
    public void init() throws IOException {
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setDescription("New Entity, created at " + LocalDate.now());
        demoEntityRepository.save(demoEntity);
        // Load Release Info from Build which was set with Maven during build
        this.releaseInfo = new String(this.getClass().getResourceAsStream("/release-info.txt").readAllBytes(), StandardCharsets.UTF_8);
    }

    @GetMapping("/demo")
    public ResponseEntity<String> demo() {
        DemoEntity demoEntity = demoEntityRepository.findAll().get(0);
        return ResponseEntity.ok("Release: " + releaseInfo + ", Loaded Entity: " + demoEntity.getId() + " - " + demoEntity.getDescription());
    }
}
