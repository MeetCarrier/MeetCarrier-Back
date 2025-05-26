package com.kslj.mannam.awsS3;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file/{dirName}")
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @Operation(
            summary     = "파일 업로드",
            description = "Multipart/form-data 형식의 파일을 지정 디렉토리에 업로드합니다.",
            parameters = {
                    @Parameter(
                            name        = "dirName",
                            description = "S3 버킷 내 저장할 디렉토리 이름",
                            required    = true,
                            in          = ParameterIn.PATH,
                            schema      = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description  = "업로드 성공",
                            content      = @Content(
                                    mediaType = "application/json",
                                    schema    = @Schema(type = "string"),
                                    examples  = @ExampleObject(value = "\"https://bucket.s3.amazonaws.com/dirName/파일명.jpg\"")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            }
    )
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
