package th.demo.portfolio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import th.demo.portfolio.model.inbound.request.SignInRequest;
import th.demo.portfolio.model.inbound.response.SignInResponse;
import th.demo.portfolio.service.SignInService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final SignInService signInService;

    public AuthenticationController(SignInService signInService) {
        this.signInService = signInService;
    }

    @PostMapping(value = "/signIn")
    public SignInResponse signIn(@Valid @RequestBody SignInRequest request) {
        return signInService.signIn(request);
    }
}