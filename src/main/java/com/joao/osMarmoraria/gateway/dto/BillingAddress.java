package com.joao.osMarmoraria.gateway.dto;

/**
 * DTO para endereço de cobrança
 */
public class BillingAddress {

    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // Construtores
    public BillingAddress() {}

    public BillingAddress(String street, String number, String city, String state, String zipCode) {
        this.street = street;
        this.number = number;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = "BR"; // Default para Brasil
    }

    // Getters e Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // Métodos utilitários
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        if (street != null) sb.append(street);
        if (number != null) sb.append(", ").append(number);
        if (complement != null && !complement.trim().isEmpty()) {
            sb.append(" - ").append(complement);
        }
        if (neighborhood != null) sb.append(", ").append(neighborhood);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(" - ").append(state);
        if (zipCode != null) sb.append(" - ").append(zipCode);

        return sb.toString();
    }

    public boolean isComplete() {
        return street != null && !street.trim().isEmpty() &&
                number != null && !number.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && !state.trim().isEmpty() &&
                zipCode != null && !zipCode.trim().isEmpty();
    }
}


