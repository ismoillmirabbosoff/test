package com.quantumitllc.spmedia.controllers;

import com.quantumitllc.spmedia.entity.MediaMetaEntity;
import com.quantumitllc.spmedia.serializers.response.MetaData;
import com.quantumitllc.spmedia.services.MediaMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files/meta")
public class MediaMetaController {

  private final MediaMetaService mediaMetaService;

  @GetMapping
  public ResponseEntity<MetaData> getByKey(@RequestParam String key) {
    return ResponseEntity.ok(mediaMetaService.getRecord(key));
  }

}
