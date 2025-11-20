package tn.esprit.docsbackend.utils.seed;

public final class SeedConstants {

    private SeedConstants() {
        // utility class
    }

    public static final String DEFAULT_PASSWORD = "123123";

    public static final int PATIENT_COUNT = 20;

    public static final int DOCTOR_COUNT = 20;

    /**
     * How many patients each doctor will be linked to in demo data.
     */
    public static final int PATIENTS_PER_DOCTOR = 5;
}
