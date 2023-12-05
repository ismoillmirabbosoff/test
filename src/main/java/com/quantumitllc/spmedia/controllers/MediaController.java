package com.quantumitllc.spmedia.controllers;

import com.amazonaws.services.s3.model.S3Object;
import com.quantumitllc.spmedia.serializers.requests.MultipleKeysRequestModel;
import com.quantumitllc.spmedia.serializers.response.DeletedObjectsResponseModel;
import com.quantumitllc.spmedia.serializers.response.MultiplePreSignedUrlResponse;
import com.quantumitllc.spmedia.serializers.response.PreSignedUrlResponse;
import com.quantumitllc.spmedia.services.MediaService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/files")
public class MediaController {

  private final MediaService mediaService;

  //--------------------------Upload single file [With Authorization header]-----------------------
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PreSignedUrlResponse> uploadNewFile(
      @RequestHeader("userId") String userId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "false") boolean isPrivate,
      @RequestParam(required = false) String folder,
      @RequestParam(defaultValue = "false") boolean meta) {

    var response = mediaService.uploadFile(file, isPrivate, meta, folder, userId);
    URI location = URI.create(response.getPreSignedUrl());
    return ResponseEntity.created(location).body(response);
  }

  //----------------------Delete a single file [Only owner with Authorization header]----------------
  @DeleteMapping
  public ResponseEntity<Void> removeSingleFile(@RequestParam String key) {
    mediaService.removeFile(key);
    return ResponseEntity.ok().build();
  }

  //----------------------Delete multiple files [Only owner with Authorization header]--------------
  @PostMapping(path = "/bulk-delete")
  public ResponseEntity<DeletedObjectsResponseModel> removeMultipleFile(@RequestBody @Valid
  MultipleKeysRequestModel body) {
    int count = mediaService.removeMultipleFiles(body.getKeys(), body.isIgnorePartialErrors());
    return ResponseEntity.ok().body(new DeletedObjectsResponseModel(count));
  }

  //----------------------Download file [Only owner with Authorization header]--------------------
  @GetMapping
  public void downloadSingleFileSecure(@RequestParam String key, HttpServletResponse response) {
    try {
      S3Object object = mediaService.retrieveFile(key);
      var content = object.getObjectContent();
      response.setContentType(object.getObjectMetadata().getContentType());
      response.setContentLengthLong(object.getObjectMetadata().getContentLength());
      IOUtils.copy(content, response.getOutputStream());
      response.flushBuffer();
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Can not download the file");
    }
  }

  //----------------------Get pre-signed url [Only owner with Authorization header]--------------
  @GetMapping(path = "/urls")
  public ResponseEntity<PreSignedUrlResponse> getPreSignedUrl(@RequestParam String key) {
    PreSignedUrlResponse response = mediaService.getFileUrl(key);
    return ResponseEntity.ok().body(response);
  }

  //----------------------Pre-sign multiple urls [Only owner with Authorization header]--------------
  @PostMapping(path = "/urls")
  public ResponseEntity<MultiplePreSignedUrlResponse> getPreSignedUrlForMultipleKeys(
      @Valid @RequestBody MultipleKeysRequestModel body) {
    var preSignedList = mediaService.getUrlsForMultipleFiles(
        body.getKeys(),
        body.isIgnorePartialErrors()
    );
    MultiplePreSignedUrlResponse res = new MultiplePreSignedUrlResponse(preSignedList);
    return ResponseEntity.ok().body(res);
  }

}
