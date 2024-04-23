package il.tsv.test.playersservice.controller;

import il.tsv.test.playersservice.error.ErrorsPresentation;
import il.tsv.test.playersservice.dto.PlayerDTO;
import il.tsv.test.playersservice.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller class for handling HTTP requests related to players.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class  PlayerController {
    private MessageSource messageSource;
    private PlayerService playerService;

    /**
     * Retrieves a list of all players.
     *
     * @return List of PlayerDTO representing all players.
     */
    @GetMapping("/all")
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return new ResponseEntity<>(playerService.getAllPlayers(),HttpStatus.OK);
    }

    /**
     * Retrieves a paginated list of players.
     *
     * @param page Page number for pagination - optional (default: 0).
     * @param size Page size for pagination -optional (default: 20).
     * @return Response containing a Page of PlayerDTO.
     */
    @GetMapping("/players")
    public ResponseEntity<?> getPageablePlayers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            //@RequestParam(required = false) String sort,
            @RequestHeader(required = false, defaultValue = "en_US") Locale locale) {
        if (page < 0) {
            final var message = messageSource
                    .getMessage("illegal.page.value", new Object[]{}, locale);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorsPresentation(List.of(message)));
        }
        if (size <= 0) {
            final var message = messageSource
                    .getMessage("illegal.size.value", new Object[]{}, locale);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorsPresentation(List.of(message)));
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayerDTO> playerPage = playerService.getAllPlayers(pageable);
        return new ResponseEntity<>(playerPage, HttpStatus.OK);
    }

    /**
     * Fetch a player by their ID.
     *
     * @param id     ID of the player.
     * @param locale Locale for retrieving localized error messages (default: en_US).
     * @return ResponseEntity containing PlayerDTO if found, otherwise returns  404 response with a localized error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerById(@PathVariable String id, @RequestHeader(required = false, defaultValue = "en_US") Locale locale) {

        PlayerDTO dto = playerService.getPlayerById(id);
        if (dto == null) {
            final var message = Optional.ofNullable(messageSource
                    .getMessage("player.not.found", new String[]{id},"Player not found", locale)).orElse("Player not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorsPresentation(List.of(message)));
        } else {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);
        }
    }


}
