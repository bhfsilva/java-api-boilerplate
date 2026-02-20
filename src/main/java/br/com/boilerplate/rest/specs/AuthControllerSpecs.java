package br.com.boilerplate.rest.specs;

import br.com.boilerplate.dtos.auth.input.LoginInputDTO;
import br.com.boilerplate.dtos.auth.input.RequirePasswordRecoveryInputDTO;
import br.com.boilerplate.dtos.auth.input.ValidatePasswordRecoveryCodeInputDTO;
import br.com.boilerplate.dtos.auth.output.LoginOutputDTO;
import br.com.boilerplate.dtos.user.input.ChangePasswordInputDTO;
import br.com.boilerplate.errors.ExceptionCode;
import br.com.boilerplate.rest.specs.commons.ApiResponseBadRequest;
import br.com.boilerplate.rest.specs.commons.ApiResponseBusinessRuleException;
import br.com.boilerplate.rest.specs.commons.ApiResponseInternalServerError;
import br.com.boilerplate.rest.specs.commons.ApiResponseNotFound;
import br.com.boilerplate.rest.specs.commons.ApiResponseUnauthorized;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ApiResponseBadRequest
@ApiResponseInternalServerError
@Tag(name = "1. Auth", description = "User access operations")
public interface AuthControllerSpecs {

    @Operation(summary = "Login")
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Ok", content = {
        @Content(
            schema = @Schema(implementation = LoginOutputDTO.class),
            mediaType = MediaType.APPLICATION_JSON_VALUE)
        }
    )
    @ResponseStatus(HttpStatus.OK)
    LoginOutputDTO login(@RequestBody LoginInputDTO request);

    @Operation(summary = "Require Password Recovery")
    @ApiResponseNotFound
    @ApiResponse(responseCode = "204", description = "No Content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void requirePasswordRecovery(@RequestBody RequirePasswordRecoveryInputDTO request);

    @Operation(summary = "Validate Password Recovery Code")
    @ApiResponseNotFound
    @ApiResponseBusinessRuleException(exceptions = ExceptionCode.INVALID_PASSWORD_RECOVERY_CODE)
    @ApiResponse(responseCode = "204", description = "No Content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void validatePasswordRecoveryCode(@RequestBody ValidatePasswordRecoveryCodeInputDTO request);

    @Operation(summary = "Change password")
    @ApiResponseNotFound
    @ApiResponseBadRequest
    @ApiResponseBusinessRuleException(exceptions = ExceptionCode.INVALID_PASSWORD_RECOVERY_CODE)
    @ApiResponse(responseCode = "204", description = "No Content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@RequestBody ChangePasswordInputDTO request);
}
