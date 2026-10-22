package com.alertamujer.devices.api;

import java.util.List;

/**
 * Se entró a una cuenta desde un celular nuevo. Lleva los tokens push de los otros celulares de la
 * usuaria para avisarles, por si no fue ella.
 *
 * @param celular nombre del celular nuevo ("Galaxy de Ana", "Samsung A54"...)
 */
public record NewPhoneSignedInEvent(@com.fasterxml.jackson.annotation.JsonProperty("celular") String phone, @com.fasterxml.jackson.annotation.JsonProperty("otrosTokens") List<String> otherTokens) {}
