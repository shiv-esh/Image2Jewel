package com.jewel.image2jewel.service;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * Custom DJL translator that preprocesses images for ResNet-50
 * and returns the raw output tensor as a float[] embedding vector
 * (instead of Classifications).
 */
public class ImageFeatureTranslator implements Translator<Image, float[]> {

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(224, 224))
                .add(new ToTensor())
                .add(new Normalize(
                        new float[]{0.485f, 0.456f, 0.406f},
                        new float[]{0.229f, 0.224f, 0.225f}));
        return pipeline.transform(new NDList(array));
    }

    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        // The model outputs a 1000-dim classification vector from ResNet-50.
        // We use this as the embedding for similarity search.
        NDArray output = list.singletonOrThrow();
        return output.toFloatArray();
    }
}
