package com.guang.campuspicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.guang.campuspicturebackend.api.imagesearch.model.ImageSearchResult;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.internal.http.HttpMethod;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author L.
 * @Date 2026/1/6 12:50
 * @Description TODO
 * @Version 1.0
 */
@Slf4j
@Deprecated
public class GetImagePageUrlApi {

    /**
     * 获取图片页面地址
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 构造请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        long uptime = System.currentTimeMillis();
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        // 发起请求
        try (HttpResponse response = HttpRequest.post(url)
                .form(formData)
                .timeout(5000)
                .execute()) {
            // 判断响应状态
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                throw new CustomException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String body = response.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new CustomException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = String.valueOf(data.get("url"));
            String searchResultUrl = URLUtil.decode(rawUrl);
            if (searchResultUrl == null) {
                throw new CustomException(ErrorCode.OPERATION_ERROR);
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("以图搜图失败：", e);
            throw new CustomException(ErrorCode.OPERATION_ERROR);
        }
    }

}
