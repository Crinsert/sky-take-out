package com.sky.controller.admin;
import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.EnvironmentVariableCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.*;
import com.aliyun.sdk.service.oss2.paginator.ListBucketsIterable;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@RestController
@RequestMapping("admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssProperties aliOssProperties;
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}", file);
        AliOssUtil aliyunOSSOperator = new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
        String extendtion = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extendtion;
        String url = aliyunOSSOperator.upload(file.getBytes(), fileName);
        return Result.success(url);
    }
}
