package com.alertamujer.alerts.application.port.in;

/** Al eliminarse una cuenta se borran los datos personales que guarda Alert Service. */
public interface BorrarDatosDeUsuariaUseCase {

    void borrarDatosDe(Long userProfileId);
}
