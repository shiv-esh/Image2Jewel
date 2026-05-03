package com.jewel.image2jewel.service;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class EmbeddingService {

    private ZooModel<Image, float[]> model;

    @PostConstruct
    public void init() throws ModelException, IOException {
        // Load ResNet-50 from PyTorch model zoo with our custom translator
        // that returns raw float[] instead of Classifications
        Criteria<Image, float[]> criteria = Criteria.builder()
                .setTypes(Image.class, float[].class)
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .optFilter("layers", "50")
                .optEngine("PyTorch")
                .optTranslator(new ImageFeatureTranslator())
                .optProgress(new ProgressBar())
                .build();

        this.model = criteria.loadModel();
    }

    public float[] extractEmbeddings(InputStream imageStream) throws IOException, TranslateException {
        Image img = ImageFactory.getInstance().fromInputStream(imageStream);
        try (Predictor<Image, float[]> predictor = model.newPredictor()) {
            return predictor.predict(img);
        }
    }
}
