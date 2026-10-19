package com.xlj.mall.common;

import java.util.List;

/**
 * 分页结果。
 */
public record PageResult<T>(List<T> list, long total, int page, int size) {
}
