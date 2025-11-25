package tn.esprit.docsbackend.utils.seed;

public final class SeedConstants {

    private SeedConstants() {
        // utility class
    }

    public static final String DEFAULT_PASSWORD = "123123";

    // 👉 50 patients and 50 doctors
    public static final int PATIENT_COUNT = 50;

    public static final int DOCTOR_COUNT = 50;

    /**
     * How many patients each doctor will be linked to in demo data
     * (at least this number).
     */
    public static final int PATIENTS_PER_DOCTOR = 5;
}
