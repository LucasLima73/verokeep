package io.verokeep.translator;

import io.verokeep.detector.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackageTranslator {

    private final Map<String, Map<String, String>> packageMap;

    public PackageTranslator(Map<String, Map<String, String>> packageMap) {
        this.packageMap = packageMap;
    }

    public TranslationResult translate(List<String> sourcePackages, PackageManager sourcePm, PackageManager targetPm) {
        String sourceKey = sourcePm.name().toLowerCase();
        String targetKey = targetPm.name().toLowerCase();

        List<String> resolved = new ArrayList<>();
        List<String> unmapped = new ArrayList<>();

        for (String sourcePackage : sourcePackages) {
            Map<String, String> entry = findEntryByValue(sourceKey, sourcePackage);
            String targetPackage = entry == null ? null : entry.get(targetKey);

            if (targetPackage == null) {
                resolved.add(sourcePackage);
                unmapped.add(sourcePackage);
            } else {
                resolved.add(targetPackage);
            }
        }

        return new TranslationResult(resolved, unmapped);
    }

    private Map<String, String> findEntryByValue(String key, String value) {
        for (Map<String, String> entry : packageMap.values()) {
            if (value.equals(entry.get(key))) {
                return entry;
            }
        }
        return null;
    }

    public record TranslationResult(List<String> packages, List<String> unmapped) {
    }
}
