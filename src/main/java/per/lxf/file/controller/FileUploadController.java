/**
 * Copyright (C), 2015-2018, XXX有限公司
 */
package per.lxf.file.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * <pre>
 * @author:      李晓福
 * @date:        2018/12/22 13:14
 * @description: 
 * @since:       1.0.0
 * @history:
 * 作者姓名       修改时间             版本号           描述
 * lxf           2018/12/22 13:14     1.0.0           创建
 * </pre>
 */

@Controller
public class FileUploadController {

    private Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    /**
     * <pre>
     * @author      lxf
     * @description 单文件上传
     * 使用springMCV注解方式注入 使用CommonsMultipartFile解析文件
     * @date        2018/12/22
     * @methdName   singleFileUpload
     * @param       file, request
     * @returnType  java.lang.String
     * </pre>
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void singleFileUpload(
            @RequestParam("file") CommonsMultipartFile file,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        JSONObject result = new JSONObject(true);
        JSONObject data = new JSONObject(true);
        if (file.getSize() > 0L) {
            String fileName = file.getOriginalFilename();

            String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
            fileName = UUID.randomUUID().toString() + "." + fileType;
            String desPath = request.getRealPath("/file/");
            File tempFile = new File(desPath, fileName);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = file.getInputStream();
                outputStream = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int size;
                do {
                    size = inputStream.read(buffer);
                    if (size != -1) {
                        outputStream.write(buffer, 0, size);
                    }
                } while (size != -1);
                result.put("code", 200);
                data.put("msg", "success");
            } catch (FileNotFoundException e) {
                result.put("code", 500);
                data.put("msg", "fail");
                logger.debug("FileNotFoundException", e.getCause());
            } catch (IOException e) {
                result.put("code", 500);
                data.put("msg", "fail");
                logger.debug("IOException", e.getCause());
            } finally {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    logger.debug("IOException", e.getCause());
                }
            }
        } else {
            result.put("code", 400);
            data.put("msg", "文件为空");
        }
        result.put("data", data);
        try {
            response.getWriter().write(result.toJSONString());
        } catch (IOException e) {
            logger.debug("IOException", e.getCause());
        }
    }

    @RequestMapping(value = "/batchUpload", method = RequestMethod.POST)
    public void batchFileUpload(
            @RequestParam("file") CommonsMultipartFile[] files,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        String desPath = request.getRealPath("/file/");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        JSONObject result = new JSONObject(true);
        JSONObject data = new JSONObject(true);
        JSONArray datas = new JSONArray(0);

        for (CommonsMultipartFile file : files) {
            if (file.getSize() > 0L) {
                String fileName = file.getOriginalFilename();
                String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
                fileName = UUID.randomUUID().toString() + "." + fileType;
                File tempFile = new File(desPath, fileName);
                try {
                    inputStream = file.getInputStream();
                    outputStream = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[1024];
                    int size;
                    do {
                        size = inputStream.read(buffer);
                        if (size != -1) {
                            outputStream.write(buffer, 0, size);
                        }
                    } while (size != -1);
                    JSONObject jsonObject = new JSONObject(true);
                    jsonObject.put("fileName", fileName);
                    jsonObject.put("fileType", fileType);
//                    jsonObject.put("fileUrl", "file/" + fileName);
                    jsonObject.put("fileUrl", "downloadFile?fileName=" + fileName);
                    datas.add(jsonObject);
                } catch (FileNotFoundException e) {
                    logger.debug("FileNotFoundException", e.getCause());
                } catch (IOException e) {
                    logger.debug("IOException", e.getCause());
                }
            } else {
                continue;
            }
        }

        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            logger.debug("IOException", e.getCause());
        } catch (NullPointerException e) {
            logger.info("NullPointerException", e);
        }
        if (datas.size() > 0) {
            result.put("code", 200);
            data.put("msg", "上传成功");
            data.put("datas", datas);
        } else {
            result.put("code", 400);
            data.put("msg", "文件为空");
        }
        result.put("data", data);
        try {
            response.getWriter().write(result.toJSONString());
        } catch (IOException e) {
            logger.debug("IOException", e.getCause());
        }
    }

    public String singleFileUpload(
            HttpServletRequest request, HttpServletResponse response) {
        JSONObject object = new JSONObject(true);
        JSONObject data = new JSONObject(true);
        String desPath = request.getRealPath("/file/");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload sfUpload = new ServletFileUpload(factory);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        List<FileItem> fileItems;
        try {
            fileItems = sfUpload.parseRequest(request);
            FileItem fileItem = fileItems.get(0);
            String fileName = fileItem.getName();
            File tempFile = new File(desPath, fileName);
            inputStream = fileItem.getInputStream();
            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int size;
            do {
                size = inputStream.read(buffer);
                if (size != -1) {
                    outputStream.write(buffer, 0, size);
                }
            } while (size != -1);
            object.put("code", 200);
            data.put("msg", "success");
        } catch (FileUploadException e) {
            object.put("code", 500);
            data.put("msg", "fail");
            logger.debug("FileUploadException", e.getCause());
        } catch (FileNotFoundException e) {
            object.put("code", 500);
            data.put("msg", "fail");
            logger.debug("FileNotFoundException", e.getCause());
        } catch (IOException e) {
            object.put("code", 500);
            data.put("msg", "fail");
            logger.debug("IOException", e.getCause());
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                logger.debug("IOException", e.getCause());
            }
        }
        object.put("data", data);
        return data.toJSONString();
    }
}
