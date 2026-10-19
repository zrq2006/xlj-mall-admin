package com.xlj.mall.dto;

import java.util.List;

public record ImportResult(int success, int failed, List<String> errors) {
}
