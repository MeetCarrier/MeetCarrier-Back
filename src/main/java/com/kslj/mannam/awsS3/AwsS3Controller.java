package com.kslj.mannam.awsS3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file/{dirName}")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam(value = "multipartFile") MultipartFile multipartFile,
                                             @PathVariable(value = "dirName") String dirName){
        return ResponseEntity.ok(awsS3Service.uploadFile(multipartFile, dirName));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam(value = "fileName") String fileName) {
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }

}
