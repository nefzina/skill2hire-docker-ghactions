package tech.wcs.dockerdemo.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.wcs.dockerdemo.persistence.entities.DemoEntity;
import tech.wcs.dockerdemo.persistence.repositories.DemoEntityRepository;

import java.time.LocalDate;

@RestController
public class DemoController {

    private final DemoEntityRepository demoEntityRepository;

    public DemoController(DemoEntityRepository demoEntityRepository) {
        this.demoEntityRepository = demoEntityRepository;
    }

    @PostConstruct
    public void init() {
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setDescription("New Entity, created at " + LocalDate.now());
        demoEntityRepository.save(demoEntity);
    }

    @GetMapping("/demo")
    public ResponseEntity<String> demo() {
        DemoEntity demoEntity = demoEntityRepository.findAll().get(0);
        return ResponseEntity.ok("Loaded Entity: " + demoEntity.getId() + " - " + demoEntity.getDescription());
    }
}
