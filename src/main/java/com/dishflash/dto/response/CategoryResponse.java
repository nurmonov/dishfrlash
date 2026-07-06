package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
}
