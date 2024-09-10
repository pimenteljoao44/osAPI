package com.joao.osMarmoraria.domain.enums;

public enum UnidadeDeMedida {

	METRO(0,"Metro"),
	CENTIMETRO(1,"Centímetro"),
	UNIDADE(2,"Unidade"),
	QUILO(3,"Quilo");

	private Integer cod;
	private String descricao;
	private UnidadeDeMedida(Integer cod, String descricao) {
		this.cod = cod;
		this.descricao = descricao;
	}
	public Integer getCod() {
		return cod;
	}

	public String getDescricao() {
		return descricao;
	}
	
	public static UnidadeDeMedida toEnum(Integer cod) {
		if(cod == null) {
			return null;
		}
		
		for(UnidadeDeMedida p : UnidadeDeMedida.values()) {
			if(cod.equals(p.getCod())) {
				return p;
			}
		}
		throw new IllegalArgumentException("Unidade de medida invalida! "+cod);
	}
	
	
}
