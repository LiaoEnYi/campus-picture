package com.guang.campuspicturebackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author L.
 * @Date 2025/12/16 16:16
 * @Description 通用删除请求
 * @Version 1.0
 */
@Data
public class DeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

}
