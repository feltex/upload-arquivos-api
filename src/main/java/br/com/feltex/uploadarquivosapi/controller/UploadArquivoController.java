package br.com.feltex.uploadarquivosapi.controller;

import br.com.feltex.uploadarquivosapi.controller.dto.ClienteDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/upload", produces = {"application/json"})
@Slf4j
@CrossOrigin("*")
public class UploadArquivoController {

    private final String pathArquivos;
    private final ObjectMapper mapper = new ObjectMapper();

    public UploadArquivoController(@Value("${app.path.arquivos}") String pathArquivos) {
        this.pathArquivos = pathArquivos;
    }

    @PostMapping("/arquivo")
    public ResponseEntity<String> salvarArquivo(@RequestParam("file") MultipartFile file) {
        log.info("Recebendo o arquivo: ", file.getOriginalFilename());
        var caminho = pathArquivos + UUID.randomUUID() + "." + extrairExtensao(file.getOriginalFilename());

        log.info("Novo nome do arquivo: " + caminho);

        try {
            Files.copy(file.getInputStream(), Path.of(caminho), StandardCopyOption.REPLACE_EXISTING);
            return new ResponseEntity<>("{ \"mensagem\": \"Arquivo carregado com sucesso!\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erro ao processar arquivo", e);
            return new ResponseEntity<>("{ \"mensagem\": \"Erro ao carregar o arquivo!\"}", HttpStatus.OK);
        }
    }


    @PostMapping("/cliente")
    public ResponseEntity<String> salvarArquivoComMetadata(@RequestParam("file") final MultipartFile file,
                                                           @RequestParam("cliente") final String clienteData) throws JsonProcessingException {
        log.info("Recebendo arquivo " + file.getOriginalFilename());

        final var cliente = mapper.readValue(clienteData, ClienteDto.class);

        log.info("Dados do cliente {}", cliente);

        var caminho = MessageFormat.format("{0}{1}-{2}.{3}", pathArquivos,
                cliente.getNome(), UUID.randomUUID(), extrairExtensao(file.getOriginalFilename()));
        try {
            Files.copy(file.getInputStream(), Path.of(caminho), StandardCopyOption.REPLACE_EXISTING);
            return new ResponseEntity<>("{ \"message\": \"Arquivo carregado com Sucesso!\"}", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("{ \"message\":  \"Erro ao carregar o arquivo!\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        int i = nomeArquivo.lastIndexOf(".");
        return nomeArquivo.substring(i + 1);
    }

}
