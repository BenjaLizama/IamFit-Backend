package com.iam_fit.ms_rutinas.controller;

import com.iam_fit.ms_rutinas.dto.UploadResponseDto;
import com.iam_fit.ms_rutinas.service.IngresarLibrosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/libros")
public class DocumentController {

    private final IngresarLibrosService ingresarLibrosService;

    public DocumentController(IngresarLibrosService ingresarLibrosService) {
        this.ingresarLibrosService = ingresarLibrosService;
    }

    @PostMapping("/cargar")
    public ResponseEntity<UploadResponseDto> cargarLibro(@RequestParam("file") MultipartFile file){
        UploadResponseDto responseDto = ingresarLibrosService.cargarLibro(file);
            return ResponseEntity.ok(responseDto);
    }
}