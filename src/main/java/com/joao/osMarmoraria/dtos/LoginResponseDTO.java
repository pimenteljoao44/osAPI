package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.enums.NivelAcesso;

public record LoginResponseDTO(Integer id, String nome, String login, String senha, NivelAcesso nivelAcesso,
                               String token) {
}
