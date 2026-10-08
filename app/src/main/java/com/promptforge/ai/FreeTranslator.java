package com.promptforge.ai;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public final class FreeTranslator {
    public interface CB { void done(boolean ok, String value); }

    private Translator translator;

    public void translateArabicToEnglish(String text, CB cb) {
        final String source = text == null ? "" : text.trim();
        if (source.isEmpty() || !looksArabic(source)) {
            cb.done(true, source);
            return;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ARABIC)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();

        translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> translator.translate(source)
                        .addOnSuccessListener(result -> {
                            close();
                            cb.done(result != null && !result.trim().isEmpty(), result == null ? source : result.trim());
                        })
                        .addOnFailureListener(e -> {
                            close();
                            cb.done(false, source);
                        }))
                .addOnFailureListener(e -> {
                    close();
                    cb.done(false, source);
                });
    }

    private boolean looksArabic(String value) {
        return value.matches("(?s).*[؀-ۿ].*");
    }

    private void close() {
        if (translator != null) {
            translator.close();
            translator = null;
        }
    }
}
