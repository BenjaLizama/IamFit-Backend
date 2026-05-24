package com.iamfit.autenticacion_seguridad.exception;

import com.iamfit.autenticacion_seguridad.dto.StandarErrorResponse;
import com.iamfit.autenticacion_seguridad.mapper.ErrorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestController.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ErrorMapper errorMapper;

    @BeforeEach
    void setup() {
        StandarErrorResponse dummyResponse = StandarErrorResponse.builder().build();

        when(errorMapper.toValidationResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toBusinessResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toAuthResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toMalformedRequestResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toAccessDeniedResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toMethodNotSupportedResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toSystemErrorResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toAccountDisabledResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toStrategyNotImplementedResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toInvalidPasswordResponse(any(), anyString())).thenReturn(dummyResponse);
        when(errorMapper.toConcurrencyConflictResponse(any(), anyString())).thenReturn(dummyResponse);
    }

    @RestController
    static class TestController {
        @GetMapping("/err/1") void err1() throws MethodArgumentNotValidException { throw mock(MethodArgumentNotValidException.class); }
        @GetMapping("/err/2") void err2() { throw new IllegalArgumentException(); }
        @GetMapping("/err/3") void err3() { throw new BadCredentialsException(""); }
        @GetMapping("/err/4") void err4() { throw mock(HttpMessageNotReadableException.class); }
        @GetMapping("/err/5") void err5() { throw new AccessDeniedException(""); }
        @GetMapping("/err/6") void err6() throws HttpRequestMethodNotSupportedException { throw new HttpRequestMethodNotSupportedException("GET"); }
        @GetMapping("/err/7") void err7() { throw new EmailAlreadyExistsException(""); }
        @GetMapping("/err/8") void err8() { throw new RoleNotFoundException(""); }
        @GetMapping("/err/9") void err9() { throw new RefreshTokenNotFoundException(""); }
        @GetMapping("/err/10") void err10() { throw new CredentialNotFoundException(""); }
        @GetMapping("/err/11") void err11() { throw new RsaKeyInitializationException("", null); }
        @GetMapping("/err/12") void err12() { throw new DisabledException(""); }
        @GetMapping("/err/13") void err13() { throw new SecurityException(""); }
        @GetMapping("/err/14") void err14() { throw new UnsupportedAuthenticationProviderException(""); }
        @GetMapping("/err/15") void err15() { throw new InvalidPasswordException(""); }
        @GetMapping("/err/16") void err16() { throw new jakarta.persistence.OptimisticLockException(""); }
        @GetMapping("/err/0") void  err0() { throw new RuntimeException(); }
    }

    @Test
    @DisplayName("Cobertura 100% de GlobalExceptionHandler")
    void testEverySingleExceptionHandler() throws Exception {
        mockMvc.perform(get("/err/1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/err/2")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/err/3")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/err/4")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/err/5")).andExpect(status().isForbidden());
        mockMvc.perform(get("/err/6")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/err/7")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/err/8")).andExpect(status().isInternalServerError());
        mockMvc.perform(get("/err/9")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/err/10")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/err/11")).andExpect(status().isInternalServerError());
        mockMvc.perform(get("/err/12")).andExpect(status().isForbidden());
        mockMvc.perform(get("/err/13")).andExpect(status().isForbidden());
        mockMvc.perform(get("/err/14")).andExpect(status().is(422));
        mockMvc.perform(get("/err/15")).andExpect(status().is(422));
        mockMvc.perform(get("/err/16")).andExpect(status().isConflict());
        mockMvc.perform(get("/err/0")).andExpect(status().isInternalServerError());

    }
}
