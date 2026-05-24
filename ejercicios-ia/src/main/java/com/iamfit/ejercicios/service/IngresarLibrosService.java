package com.iamfit.ejercicios.service;

import com.iamfit.ejercicios.dto.UploadResponseDto;
import com.iamfit.ejercicios.exception.DocumentException;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngresarLibrosService {
   private final VectorStore vectorStore;

    public IngresarLibrosService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }


    public UploadResponseDto cargarLibro(MultipartFile archivoPdf){

        if(archivoPdf.isEmpty()){
            throw new DocumentException("el archivo esta vacio");

        }
        String tipo = archivoPdf.getContentType();

        if (!"application/pdf".equals(tipo)){
            throw new DocumentException("Solo se permiten archivos pdf");
        }

        Resource resource = archivoPdf.getResource();
        String nombre = archivoPdf.getOriginalFilename();

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);

        var paginas = pdfReader.get();
        int totalPaginas = paginas.size();

        if(totalPaginas > 10){
            throw new DocumentException("Solo se permiten archivos con maximo de 10 paginas");
        }

        TokenTextSplitter textSplitter = new TokenTextSplitter();

        var documentos = textSplitter.apply(paginas);

        this.vectorStore.add(documentos);

        return new UploadResponseDto("libro vectorizado correctamente",nombre);
    }
}
