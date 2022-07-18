package com.example.demo.api;

import com.example.demo.dto.ArticleDto;
import com.example.demo.dto.ArticleQueryCondition;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@Tag(name = "Article Api")
@RestController("/article")
public class ArticleApi {

    @GetMapping("query")
    public List<ArticleDto> query(ArticleQueryCondition condition) {
        return new LinkedList<>();
    }

    @PostMapping(value = "create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ArticleDto create(ArticleDto dto, @RequestPart MultipartFile file) {
        return new ArticleDto();
    }
}
