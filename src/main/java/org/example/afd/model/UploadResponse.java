package org.example.afd.model;

import lombok.Data;

@Data
public class UploadResponse {
    private Integer code; //编码：1成功，0为失败
    private String message; //错误信息
    private String url; //数据
    public static UploadResponse success() {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.code = 1;
        uploadResponse.message = "success";
        return uploadResponse;
    }

    public static UploadResponse success(String url) {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.code = 1;
        uploadResponse.message = "success";
        uploadResponse.url = url;
        return uploadResponse;
    }

    public static UploadResponse error(String msg) {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.code = 0;
        uploadResponse.message = msg;
        return uploadResponse;
    }
}
