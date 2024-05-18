package il.tsv.test.playersservice.error;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class ExceptionHelper {


    @ExceptionHandler(value = {SQLException.class})
    public ResponseEntity<Object> handleSQLException(SQLException ex) {
        log.error("SQLException ", ex);
        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorsPresentation(List.of("Internal Server Error.", "Contact the development team.")));
    }

    @ExceptionHandler(value = {PulsarClientException.class})
    public ResponseEntity<Object> handlePulsarClientException(PulsarClientException ex) {
        log.error("SQLException ", ex);
        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorsPresentation(List.of("PulsarClientException.", ex.getMessage())));
    }
    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException ", ex);
        String msg= Optional.ofNullable(ex.getMessage()).orElse("Internal Server Error. Contact the development team.");

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorsPresentation(List.of(msg)));
    }



}

