package cc.sighs.auieditor.editor.ore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sighs.apricityui.ui.UiTranslations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Supplies editor accessibility labels when Minecraft translations are unavailable. */
final class OreEditorTranslations {
    private static final Map<String, String> ENGLISH = loadEnglish();

    private OreEditorTranslations() {
    }

    static String translate(String key) {
        String translated = UiTranslations.translate(key);
        return key.equals(translated) ? ENGLISH.getOrDefault(key, key) : translated;
    }

    private static Map<String, String> loadEnglish() {
        try (InputStream stream = OreEditorTranslations.class.getResourceAsStream("/assets/auieditor/lang/en_us.json")) {
            if (stream == null) return Map.of();
            return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>() {}.getType());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load editor translations", exception);
        }
    }
}
