package tn.esprit.docsbackend.utils.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.docsbackend.entities.IndicatorType;
import tn.esprit.docsbackend.repositories.IndicatorTypeRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndicatorTypeDataSeeder implements DataSeeder {

    private final IndicatorTypeRepository indicatorTypeRepository;

    @Override
    @Transactional
    public void seed() {
        long count = indicatorTypeRepository.count();
        if (count > 0) {
            log.info("IndicatorTypeDataSeeder: {} indicator types already present, skipping.", count);
            return;
        }

        log.info("IndicatorTypeDataSeeder: seeding default indicator types...");

        List<IndicatorType> defaults = List.of(
                IndicatorType.builder().code("HR").name("Heart rate").unit("bpm")
                        .description("Heart beats per minute").active(true).build(),
                IndicatorType.builder().code("BP_SYS").name("Systolic blood pressure").unit("mmHg")
                        .description("Upper blood pressure value").active(true).build(),
                IndicatorType.builder().code("BP_DIA").name("Diastolic blood pressure").unit("mmHg")
                        .description("Lower blood pressure value").active(true).build(),
                IndicatorType.builder().code("GLU_FAST").name("Fasting blood glucose").unit("mg/dL")
                        .description("Fasting glycemia").active(true).build(),
                IndicatorType.builder().code("GLU_POST").name("Postprandial glucose").unit("mg/dL")
                        .description("Glucose after meal").active(true).build(),
                IndicatorType.builder().code("WEIGHT").name("Body weight").unit("kg")
                        .description("Body weight in kilograms").active(true).build(),
                IndicatorType.builder().code("HEIGHT").name("Height").unit("cm")
                        .description("Body height in centimeters").active(true).build(),
                IndicatorType.builder().code("TEMP").name("Body temperature").unit("°C")
                        .description("Body temperature").active(true).build(),
                IndicatorType.builder().code("SPO2").name("Oxygen saturation").unit("%")
                        .description("Blood oxygen saturation").active(true).build(),
                IndicatorType.builder().code("RR").name("Respiratory rate").unit("breaths/min")
                        .description("Breaths per minute").active(true).build(),
                IndicatorType.builder().code("BMI").name("Body Mass Index").unit("kg/m²")
                        .description("Calculated body mass index").active(true).build(),
                IndicatorType.builder().code("PAIN").name("Pain level").unit("0-10")
                        .description("Subjective pain score").active(true).build(),
                IndicatorType.builder().code("SUGAR_URINE").name("Urine glucose").unit("")
                        .description("Presence of glucose in urine").active(true).build(),
                IndicatorType.builder().code("CHOLESTEROL").name("Total cholesterol").unit("mg/dL")
                        .description("Total blood cholesterol").active(true).build(),
                IndicatorType.builder().code("TRIGLY").name("Triglycerides").unit("mg/dL")
                        .description("Blood triglycerides").active(true).build()
        );

        indicatorTypeRepository.saveAll(defaults);

        log.info("IndicatorTypeDataSeeder: seeded {} indicator types.", defaults.size());
    }
}
