package FileManager.FileManager.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class health {

    @GetMapping
    public String hi(){
        return "ResponseEntity.ok()";

    }
}
