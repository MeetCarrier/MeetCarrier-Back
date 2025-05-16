package com.kslj.mannam.awsS3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file/{dirName}")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile,
                                             @PathVariable("dirName") String dirName){
        return ResponseEntity.ok(awsS3Service.uploadFile(multipartFile, dirName));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam("fileName") String fileName,
                                             @PathVariable("dirName") String dirName) {
        awsS3Service.deleteFile(dirName, fileName);
        return ResponseEntity.ok(fileName);
    }

}
