package tn.esprit.docsbackend.entities.enums;

public enum AppointmentStatus {
    PENDING,    // demandé par le patient, en attente validation
    ACCEPTED,    // accepté par le docteur
    REJECTED,    // refusé par le docteur
    CANCELLED,   // annulé par le patient
    COMPLETED    // consultation terminée
}
