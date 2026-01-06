package com.guang.campuspicturebackend.api.aliyun;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import cn.hutool.json.JSONUtil;
import com.guang.campuspicturebackend.api.aliyun.model.CreateOutPaintingTaskRequest;
import com.guang.campuspicturebackend.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.guang.campuspicturebackend.api.aliyun.model.GetOutPaintingTaskResponse;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.compiler.CompileError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author L.
 * @Date 2026/1/6 13:53
 * @Description TODO
 * @Version 1.0
 */
@Slf4j
@Component
public class AliYunAiApi {
    @Value("${aliYunApi.apiKey}")
    private String apiKey;
    private static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    private static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    public CreateOutPaintingTaskResponse createPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        ThrowUtils.throwIf(createOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR);
        // 构造请求
        HttpRequest request = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        // 发起请求
        try (HttpResponse response = request.execute()) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                throw new CustomException(ErrorCode.OPERATION_ERROR);
            }
            CreateOutPaintingTaskResponse res = JSONUtil.toBean(response.body(), CreateOutPaintingTaskResponse.class);
            ThrowUtils.throwIf(res == null, ErrorCode.OPERATION_ERROR);
            String errorMessage = res.getMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                throw new CustomException(ErrorCode.OPERATION_ERROR, errorMessage);
            }
            return res;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OPERATION_ERROR);
        }
    }

    public GetOutPaintingTaskResponse getOutputTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        // 构造请求
        HttpRequest request = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header("Authorization", "Bearer " + apiKey);
        try (HttpResponse response = request.execute()) {
            if (!response.isOk()) {
                throw new CustomException(ErrorCode.OPERATION_ERROR);
            }
            GetOutPaintingTaskResponse res = JSONUtil.toBean(response.body(), GetOutPaintingTaskResponse.class);
            ThrowUtils.throwIf(res == null, ErrorCode.OPERATION_ERROR);
            return res;
        }
    }
}
