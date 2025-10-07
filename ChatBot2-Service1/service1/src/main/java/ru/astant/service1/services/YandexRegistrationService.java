package ru.astant.service1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.astant.service1.models.User;
import ru.astant.service1.repositories.UserRepository;

import java.util.Map;

@Service
public class YandexRegistrationService {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();


    @Autowired
    public YandexRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getTokenFromYandex(String code) {
        String url = "https://oauth.yandex.ru/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", "3621852775f64452a26c5cec19e36c29");
        params.add("client_secret", "ae425a906ad44da496bb1a511fe9d41e");
        params.add("redirect_uri", "http://localhost:8080/api/auth/yandex/callback");

        System.out.println("Requesting token with code: " + code); // Добавьте логирование

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            System.out.println("Yandex token response: " + response.getStatusCode() + " - " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            } else {
                throw new RuntimeException("Не удалось получить токен от Яндекса: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error getting token from Yandex: " + e.getMessage());
            throw new RuntimeException("Ошибка при получении токена: " + e.getMessage(), e);
        }
    }



    public Map<String, Object> getUserInfoFromYandex(String accessToken) {
        String url = "https://login.yandex.ru/info?format=json";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        return response.getBody();
    }


    public User creatOrFindUser(Map<String, Object> userInfo) {
        String yandexId = (String) userInfo.get("id");
        String login = (String) userInfo.get("login");

        if (yandexId == null || login == null) {
            throw new IllegalArgumentException("Некорректный ответ от Яндекс: id или login отсутствует. Ответ: " + userInfo);
        }

        return userRepository.findByYandexId(yandexId).orElseGet(() -> {
            User newUser = new User();
            newUser.setYandexId(yandexId);
            newUser.setUsername(login);
            newUser.setRegistrationType("YANDEX");
            return userRepository.save(newUser);
        });
    }

}
