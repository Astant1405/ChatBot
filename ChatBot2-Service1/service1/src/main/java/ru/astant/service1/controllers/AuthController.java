package ru.astant.service1.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.astant.service1.models.User;
import ru.astant.service1.security.JWTUtil;
import ru.astant.service1.services.RegistrationService;
import ru.astant.service1.services.YandexRegistrationService;
import ru.astant.service1.util.UserValidator;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final UserValidator userValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final YandexRegistrationService yandexRegistrationService;

    @Autowired
    public AuthController(UserValidator userValidator, RegistrationService registrationService, JWTUtil jwtUtil, AuthenticationManager authenticationManager, YandexRegistrationService yandexRegistrationService) {
        this.userValidator = userValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.yandexRegistrationService = yandexRegistrationService;
    }

    @PostMapping("/registration")
    public Map<String, Object> registration(@RequestBody @Valid User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return Map.of(
                    "success", false,
                    "message", "Ошибка: " + bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
        }
        registrationService.register(user);
        String token = jwtUtil.generateToken(user.getUsername());
        return Map.of(
                "success", true,
                "jwt-token", token,
                "username", user.getUsername(),
                "message", "Регистрация успешна"
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid User user) {
        UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        try{
            authenticationManager.authenticate(authenticationToken);
        }
        catch (BadCredentialsException e){
            return Map.of(
                    "success", false,
                    "message", "Неверное имя пользователя или пароль"
            );
        }
        String token = jwtUtil.generateToken(user.getUsername());
        return Map.of(
                "success", true,
                "jwt-token", token,
                "username", user.getUsername(),
                "message", "Вход успешен"
        );
    }

    @GetMapping("/yandex")
    public void redirectToYandexOAuth(HttpServletResponse response)throws IOException {
        String url = "https://oauth.yandex.ru/authorize?response_type=code&client_id=3621852775f64452a26c5cec19e36c29&redirect_uri=http://localhost:8080/api/auth/yandex/callback";
        response.sendRedirect(url);
    }

    @GetMapping("/yandex/callback")
    public void yandexCallbackRedirect(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        String redirectUrl = "http://localhost:8080/auth.html?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }


    @PostMapping("/yandex/login")
    public Map<String, Object> yandexCallback(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String token = yandexRegistrationService.getTokenFromYandex(code);
            Map<String, Object> userInfo = yandexRegistrationService.getUserInfoFromYandex(token);
            User user = yandexRegistrationService.creatOrFindUser(userInfo);
            String jwt = jwtUtil.generateToken(user.getUsername());
            return Map.of(
                    "success", true,
                    "jwt-token", jwt,
                    "username", user.getUsername(),
                    "message", "Вход успешен"
            );

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "Ошибка при авторизации через Яндекс: " + e.getMessage()
            );

        }
    }
}
