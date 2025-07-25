package com.joao.osMarmoraria.domain.enums;

public enum StatusOrdemServico {
    PENDENTE("Pendente", "Ordem de serviço pendente de aprovação"),
    APROVADA("Aprovada", "Ordem de serviço aprovada para execução"),
    AGENDADA("Agendada", "Ordem de serviço agendada com datas definidas"),
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

    public boolean podeTransicionarPara(StatusOrdemServico novoStatus) {
        switch (this) {
            case PENDENTE:
                return novoStatus == APROVADA || novoStatus == AGENDADA || novoStatus == CANCELADA;
            case APROVADA:
                return novoStatus == AGENDADA || novoStatus == EM_ANDAMENTO || novoStatus == CANCELADA;
            case AGENDADA:
                return novoStatus == EM_ANDAMENTO || novoStatus == CANCELADA;
            case EM_ANDAMENTO:
                return novoStatus == PAUSADA || novoStatus == CONCLUIDA || novoStatus == CANCELADA;
            case PAUSADA:
                return novoStatus == EM_ANDAMENTO || novoStatus == CANCELADA;
            case CONCLUIDA:
                return false; // Status final
            case CANCELADA:
                return false; // Status final
            default:
                return false;
        }
    }

    public boolean isFinal() {
        return this == CONCLUIDA || this == CANCELADA;
    }

    public boolean isAtiva() {
        return this == EM_ANDAMENTO || this == PAUSADA;
    }
}