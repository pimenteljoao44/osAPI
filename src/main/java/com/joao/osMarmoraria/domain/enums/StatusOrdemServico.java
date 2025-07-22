package com.joao.osMarmoraria.domain.enums;

public enum StatusOrdemServico {
    PENDENTE("Pendente", "Ordem de serviço pendente de execução"),
    EM_ANDAMENTO("Em Andamento", "Ordem de serviço em execução"),
    PAUSADA("Pausada", "Ordem de serviço pausada temporariamente"),
    CONCLUIDA("Concluída", "Ordem de serviço concluída"),
    CANCELADA("Cancelada", "Ordem de serviço cancelada");

    private final String nome;
    private final String descricao;

    StatusOrdemServico(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}

