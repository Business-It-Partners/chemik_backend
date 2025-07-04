// Data send by the user form the client to upload the product

package com.chemiki.app.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateProductRequestDTO {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private Double price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Images are required")
    private List<MultipartFile> images;
}