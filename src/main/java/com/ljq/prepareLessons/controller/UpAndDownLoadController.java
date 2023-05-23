package com.ljq.prepareLessons.controller;

import com.ljq.prepareLessons.PrepareLessons;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author 李佳琪
 * 2023-04-28
 */
@RestController
public class UpAndDownLoadController extends BaseController {

    @Value("${tempFile}")
    private String tempFile;
    @Value("${outFile}")
    private String outFile;
    @Value("${env}")
    private String env;
    @Value("${fontName}")
    private String fontName;
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "application/pdf");

    @RequestMapping(value = "/upload", consumes = "multipart/form-data", headers = "content-type=multipart/form-data", method = RequestMethod.POST)
    @ResponseBody
    public void upload(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("in。。。");
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new Exception("非法的文件类型！");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        String currTime = sdf.format(new Date());
        if ("dev".equals(env)) {
            tempFile = "C:\\Users\\Administrator\\Desktop\\";
            outFile = "C:\\Users\\Administrator\\Desktop\\";
        }
        String fileName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
        String currentReqTempFile = tempFile + fileName + "-O-" + currTime + ".jpg";
        String currentReqOutFile = outFile + fileName + "-DONE-" + currTime + ".jpg";

        try (InputStream inputStream = file.getInputStream();
             FileOutputStream tempFos = new FileOutputStream(currentReqTempFile)) {
            IOUtils.copy(inputStream, tempFos);
        }

        Thread.sleep(100);
        System.out.println("开始。。。");
        PrepareLessons pl = PrepareLessons.getInstance(currentReqTempFile, currentReqOutFile, env, fontName);
        pl.doPrepareLesson();
        Thread.sleep(100);

        System.out.println("  OVER  ");

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;fileName=" + fileName + "-DONE-" + currTime + ".jpg");

        try (FileInputStream fis = new FileInputStream(currentReqOutFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buf = new byte[512];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        } catch (Exception e) {
            System.err.println("出错了");
        }
    }

}
