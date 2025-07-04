package com.chemiki.app.dto.responseDto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private List<String> images;
    private LocalDateTime createdAt;
}