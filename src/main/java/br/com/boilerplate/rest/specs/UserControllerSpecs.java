package br.com.boilerplate.rest.specs;

import br.com.boilerplate.dtos.generics.pagination.output.PaginationOutputDTO;
import br.com.boilerplate.dtos.user.input.CreateUserInputDTO;
import br.com.boilerplate.dtos.user.input.FindUsersByFiltersInputDTO;
import br.com.boilerplate.dtos.user.output.UserDetailedOutputDTO;
import br.com.boilerplate.dtos.user.output.UserMinimalOutputDTO;
import br.com.boilerplate.rest.specs.commons.ApiResponseBadRequest;
import br.com.boilerplate.rest.specs.commons.ApiResponseDuplicatedResource;
import br.com.boilerplate.rest.specs.commons.ApiResponseForbidden;
import br.com.boilerplate.rest.specs.commons.ApiResponseInternalServerError;
import br.com.boilerplate.rest.specs.commons.ApiResponseNotFound;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ApiResponseInternalServerError
@Tag(name = "2. User", description = "User operations")
public interface UserControllerSpecs {

    abstract class UserMinimalOutputPaginationDTO extends PaginationOutputDTO<UserMinimalOutputDTO> { }

    @Operation(summary = "Find all users")
    @ApiResponseForbidden
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "200", description = "Ok", content = {
            @Content(
                    array = @ArraySchema(schema = @Schema(implementation = UserMinimalOutputPaginationDTO.class)),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
            )}
    )
    @SecurityRequirement(name = "jwt")
    @ResponseStatus(HttpStatus.OK)
    PaginationOutputDTO<UserMinimalOutputDTO> list(@ParameterObject @ModelAttribute FindUsersByFiltersInputDTO request);

    @Operation(summary = "Create user")
    @ApiResponseBadRequest
    @ApiResponseDuplicatedResource
    @ApiResponse(responseCode = "201", description = "Created")
    @ResponseStatus(HttpStatus.CREATED)
    void create(@RequestBody CreateUserInputDTO request);

    @Operation(summary = "Find user by id")
    @ApiResponseNotFound
    @ApiResponseForbidden
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "200", description = "Ok", content = {
            @Content(
                    schema = @Schema(implementation = UserDetailedOutputDTO.class),
                    mediaType = MediaType.APPLICATION_JSON_VALUE
            )}
    )
    @SecurityRequirement(name = "jwt")
    @ResponseStatus(HttpStatus.OK)
    UserDetailedOutputDTO findById(@PathVariable UUID id, @RequestParam Boolean includeInactive);

    @Operation(summary = "Validate e-mail")
    @ApiResponseNotFound
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "200", description = "Ok", content = {
            @Content(mediaType = MediaType.ALL_VALUE),
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE),
            @Content(mediaType = MediaType.TEXT_HTML_VALUE)
    })
    @ResponseStatus(HttpStatus.OK)
    String validateEmail(@PathVariable UUID id);

    @Operation(summary = "Delete user by id")
    @ApiResponseNotFound
    @ApiResponseForbidden
    @ApiResponseBadRequest
    @ApiResponse(responseCode = "204", description = "No Content")
    @SecurityRequirement(name = "jwt")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(@PathVariable UUID id);
}
