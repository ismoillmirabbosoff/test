package com.quantumitllc.spmedia.entity;

public enum FileTypes {

  JPG("jpg"),
  JPEG("jpeg"),
  PNG("png"),
  PDF("pdf"),
  DOC("doc"),
  DOCX("docx"),
  XLSX("xlsx"),
  CSV("csv");
  private final String name;

  FileTypes(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
