package com.joao.osMarmoraria.external;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class IbgeClient {

    private final RestTemplate restTemplate;

    private final String IBGE_API_URL = "https://servicodados.ibge.gov.br/api/v1/localidades";

    public IbgeClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    public List<Map<String, String>> getEstados() {
        String url = IBGE_API_URL + "/estados?orderBy=nome";
        Map[] response = restTemplate.getForObject(url, Map[].class);
        return Arrays.stream(response)
                .map(estado -> Map.of(
                      "id", estado.get("id").toString(),
                      "sigla", estado.get("sigla").toString(),
                      "nome", estado.get("nome").toString()
                ))
                .toList();
    }

    public List<Map<String, String>> getCidades(String uf) {
        String url = IBGE_API_URL + "/estados/" + uf + "/municipios";
        Map[] response = restTemplate.getForObject(url, Map[].class);
        return Arrays.stream(response)
                .map(cidade -> Map.of(
                        "id", cidade.get("id").toString(),
                        "nome", cidade.get("nome").toString(),
                        "uf", uf.toUpperCase()
                ))
                .toList();
    }
}
