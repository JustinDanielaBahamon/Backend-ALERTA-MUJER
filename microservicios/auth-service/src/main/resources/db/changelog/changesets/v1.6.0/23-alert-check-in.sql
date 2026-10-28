--liquibase formatted sql

-- La alerta ya no se cierra sola a los 30 minutos: a esa hora se le pregunta si sigue en peligro.
--   extended_until: hasta cuándo confirmó que sigue en peligro ("Sigo en peligro").
--   unanswered_at:  pasó la hora sin respuesta y se avisó a los contactos (la alerta sigue activa).
--   duress_at:      "canceló" con su PIN de coacción: en su celular parece cancelada pero sigue.
--   last_signal_at: hora del servidor de la última ubicación recibida de la alerta.
--   signal_lost_at: cuándo se avisó a los contactos que su celular dejó de enviar ubicación.

--changeset alerta-mujer-team:v1.6.0-alert-check-in
ALTER TABLE alert.alert
    ADD COLUMN extended_until TIMESTAMP,
    ADD COLUMN unanswered_at  TIMESTAMP,
    ADD COLUMN duress_at      TIMESTAMP,
    ADD COLUMN last_signal_at TIMESTAMP,
    ADD COLUMN signal_lost_at TIMESTAMP;
--rollback ALTER TABLE alert.alert DROP COLUMN signal_lost_at, DROP COLUMN last_signal_at, DROP COLUMN duress_at, DROP COLUMN unanswered_at, DROP COLUMN extended_until;
