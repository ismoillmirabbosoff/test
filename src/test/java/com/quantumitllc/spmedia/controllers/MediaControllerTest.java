package com.quantumitllc.spmedia.controllers;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.quantumitllc.spmedia.serializers.requests.MultipleKeysRequestModel;
import com.quantumitllc.spmedia.serializers.response.PreSignedUrlResponse;
import com.quantumitllc.spmedia.services.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mediaController).build();
    }

    @Test
    public void testUploadNewFile() throws Exception {
        final String fileUrl = "http://example.com/file/test.txt";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Test content".getBytes());
        PreSignedUrlResponse preSignedUrlResponse = new PreSignedUrlResponse();
        preSignedUrlResponse.setPreSignedUrl(fileUrl);
        when(mediaService.uploadFile(file, false, false,null, "userId")).thenReturn(preSignedUrlResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
                        .file(file)
                        .param("isPrivate", "false")
                .header("userId", "userId"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "http://example.com/file/test.txt"));

        verify(mediaService).uploadFile(file, false, false, null, "userId");
    }

    @Test
    public void testRemoveSingleFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/files")
                .param("key", "test.txt"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(mediaService).removeFile("test.txt");
    }

    @Test
    public void testRemoveMultipleFile() throws Exception {
        MultipleKeysRequestModel requestModel = new MultipleKeysRequestModel();
        requestModel.setKeys(Arrays.asList("file1.txt", "file2.txt"));
        requestModel.setIgnorePartialErrors(true);
        when(mediaService.removeMultipleFiles(requestModel.getKeys(), requestModel.isIgnorePartialErrors())).thenReturn(2);

        mockMvc.perform(MockMvcRequestBuilders.post("/files/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keys\":[\"file1.txt\",\"file2.txt\"],\"ignorePartialErrors\":true}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"count\":2}"));

        verify(mediaService).removeMultipleFiles(requestModel.getKeys(), requestModel.isIgnorePartialErrors());
    }

    @Test
    public void testDownloadSingleFileSecure() throws Exception {
        S3Object s3Object = new S3Object();
        S3ObjectInputStream objectContent = new S3ObjectInputStream(new ByteArrayInputStream("Test content".getBytes()), null);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(MediaType.TEXT_PLAIN_VALUE);
        objectMetadata.setContentLength(12L);
        s3Object.setObjectContent(objectContent);
        s3Object.setObjectMetadata(objectMetadata);
        when(mediaService.retrieveFile("test.txt")).thenReturn(s3Object);

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/files")
                .param("key", "test.txt"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse();

        assertEquals(MediaType.TEXT_PLAIN_VALUE, response.getContentType());
        assertEquals(12L, response.getContentLength());
        assertEquals("Test content", response.getContentAsString());

        verify(mediaService).retrieveFile("test.txt");
    }

    @Test
    public void testGetPreSignedUrl() throws Exception {
        PreSignedUrlResponse preSignedUrlResponse = new PreSignedUrlResponse();
        preSignedUrlResponse.setPreSignedUrl("http://example.com/file/test.txt");
        when(mediaService.getFileUrl("test.txt")).thenReturn(preSignedUrlResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/files/urls")
                .param("key", "test.txt"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"preSignedUrl\":\"http://example.com/file/test.txt\"}"));

        verify(mediaService).getFileUrl("test.txt");
    }

}
