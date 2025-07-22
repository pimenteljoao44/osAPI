package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.external.IbgeClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LocalidadeService {
    private final IbgeClient ibgeClient;

    public LocalidadeService(IbgeClient ibgeClient) {
        this.ibgeClient = ibgeClient;
    }

    public List<Map<String, String>> listarEstados() {
        return ibgeClient.getEstados();
    }

    public List<Map<String, String>> listarCidades(String uf) {
        return ibgeClient.getCidades(uf.toUpperCase());
    }
}
