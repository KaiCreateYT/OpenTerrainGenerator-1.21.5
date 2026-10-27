package com.pg85.otg.config.yaml;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.pg85.otg.config.settingtype.*;
import com.pg85.otg.config.settings.ConfigSection;
import com.pg85.otg.config.settings.biome.*;
import com.pg85.otg.config.settings.preset.*;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.biome.*;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.OTGLog;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class SettingsSchemaGenerator {

    private static final Map<String, Class<?>> COMPLEX_TYPE_CLASSES = new HashMap<>();

    static {
        COMPLEX_TYPE_CLASSES.put("WeightedMobSpawnGroup", WeightedMobSpawnGroup.class);
        COMPLEX_TYPE_CLASSES.put("ReplaceBlockMatrix", ReplaceBlockMatrix.class);
        COMPLEX_TYPE_CLASSES.put("ReplacedBlocksInstruction", ReplacedBlocksInstruction.class);
        COMPLEX_TYPE_CLASSES.put("ColorSet", ColorSet.class);
        COMPLEX_TYPE_CLASSES.put("ColorThreshold", ColorThreshold.class);
        COMPLEX_TYPE_CLASSES.put("Material", LocalMaterialData.class);
        COMPLEX_TYPE_CLASSES.put("MaterialSet", MaterialSet.class);
    }

    public static String generateJsonSchema(Map<String, Map<String, Setting<?>>> categorizedSettings) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

        StringBuilder jsonSchema = new StringBuilder();
        jsonSchema.append("{\n");
        jsonSchema.append("  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n");
        jsonSchema.append("  \"type\": \"object\",\n");
        jsonSchema.append("  \"title\": \"OTG Config Schema\",\n");
        jsonSchema.append("  \"description\": \"Schema for OTG config files\",\n");
        jsonSchema.append("  \"id\": \"http://openterraingen.github.io/OTGConfigSchema-"+ Constants.ConfigVersion +".json\",\n");
        jsonSchema.append("  \"properties\": {\n");

        Set<String> complexTypesUsed = new HashSet<>();

        for (Map.Entry<String, Map<String, Setting<?>>> categoryEntry : categorizedSettings.entrySet()) {
            String category = categoryEntry.getKey();
            Map<String, Setting<?>> settings = categoryEntry.getValue();

            jsonSchema.append("    \"").append(category).append("\": {\n");
            jsonSchema.append("      \"type\": \"object\",\n");
            jsonSchema.append("      \"properties\": {\n ");

            for (Map.Entry<String, Setting<?>> entry : settings.entrySet()) {
                Setting<?> setting = entry.getValue();
                if (setting == null) {
                    OTGLog.warn("Setting is null: {}", entry.getKey());
                    continue;
                }
                jsonSchema.append("        \"").append(setting.getName()).append("\": {\n");
                String type = setting.getTypeAsString();
                jsonSchema.append("          \"type\": \"").append(type).append("\",\n");
                if ("string".equals(type) && setting.getStringFormat() != null) {
                    jsonSchema.append("          \"format\": \"").append(setting.getStringFormat()).append("\",\n");
                }

                String defaultValue = setting.getDefaultValueAsString();
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    if ("string".equals(setting.getTypeAsString())) {
                        jsonSchema.append("          \"default\": \"").append(defaultValue).append("\",\n");
                    } else if ("array".equals(setting.getTypeAsString())) {
                        // wrap the default values in square brackets
                        jsonSchema.append("          \"default\": [").append(defaultValue).append("],\n");
                    } else {
                        jsonSchema.append("          \"default\": ").append(defaultValue).append(",\n");
                    }
                }

                Number minValue = setting.getMinValue();
                if (minValue != null) {
                    jsonSchema.append("          \"minimum\": ").append(minValue).append(",\n");
                }

                Number maxValue = setting.getMaxValue();
                if (maxValue != null) {
                    jsonSchema.append("          \"maximum\": ").append(maxValue).append(",\n");
                }

                List<String> enumValues = setting.getEnumValues();
                if (enumValues != null) {
                    jsonSchema.append("          \"enum\": [\n");
                    for (String value : enumValues) {
                        jsonSchema.append("            \"").append(value).append("\",\n");
                    }
                    jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last comma and newline
                    jsonSchema.append("\n          ],\n");
                }

                String[] description = setting.getDescription();
                if (description != null && description.length > 0) {
                    jsonSchema.append("          \"description\": \"");
                    for (String desc : description) {
                        jsonSchema.append(desc.replace("\"", "\\\"").replace("\t", " ")).append(" ");
                    }
                    jsonSchema.setLength(jsonSchema.length() - 1); // Remove the last space
                    jsonSchema.append("\",\n");
                }

                String complexTypeSchema = setting.getComplexTypeSchema();
                if (complexTypeSchema != null) {
                    if ("array".equals(setting.getTypeAsString())) {
                        jsonSchema.append("          \"items\": {\n");
                        if (isPrimitiveType(complexTypeSchema)) {
                            jsonSchema.append("            \"type\": \"").append(complexTypeSchema).append("\"\n");
                        } else {
                            jsonSchema.append("            \"$ref\": \"#/definitions/").append(complexTypeSchema).append("\"\n");
                            complexTypesUsed.add(complexTypeSchema);
                        }
                        jsonSchema.append("          },\n");
                    } else {
                        jsonSchema.append("          \"$ref\": \"#/definitions/").append(complexTypeSchema).append("\",\n");
                        complexTypesUsed.add(complexTypeSchema);
                    }
                }
                jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last newline
                jsonSchema.append("\n        },\n");
            }
            jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last comma and newline
            jsonSchema.append("\n      }\n");
            jsonSchema.append("    },\n");
        }

        jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last comma and newline
        jsonSchema.append("\n  },\n");

        if (!complexTypesUsed.isEmpty()) {
            jsonSchema.append("  \"definitions\": {\n");
            for (String complexType : complexTypesUsed) {
                Class<?> clazz = COMPLEX_TYPE_CLASSES.get(complexType);
                if (clazz != null) {
                    jsonSchema.append("    \"").append(complexType).append("\": {\n");
                    JsonSchema schema = schemaGen.generateSchema(clazz);
                    processSchema(jsonSchema, schema, "      ");
                    jsonSchema.append("\n    },\n");
                }
            }
            jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last comma and newline
            jsonSchema.append("\n  },\n");
        }
        jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last comma and newline
        jsonSchema.append("\n}\n");

        return jsonSchema.toString();
    }

    private static void processSchema(StringBuilder jsonSchema, JsonSchema schema, String spaces) {
        if (schema.isObjectSchema()) {
            jsonSchema.append(spaces).append("\"type\": \"object\",\n");
            jsonSchema.append(spaces).append("\"properties\": {\n");
            ObjectSchema objectSchema = schema.asObjectSchema();
            objectSchema.getProperties().forEach((key, value) -> {
                jsonSchema.append(spaces).append("  \"").append(key).append("\": {\n");
                processSchema(jsonSchema, value, spaces + "    ");
                if (key.equals("color")) {
                    jsonSchema.append(",\n").append(spaces).append("    \"format\": \"color\"");
                }
                jsonSchema.append("\n").append(spaces).append("  },\n");
            });
            jsonSchema.setLength(jsonSchema.length() - 2); // Remove the last comma and newline
            jsonSchema.append("\n").append(spaces).append("}");
        } else if (schema.isArraySchema()) {
            jsonSchema.append(spaces).append("\"type\": \"array\",\n");
            jsonSchema.append(spaces).append("\"items\": {\n");
            ArraySchema arraySchema = schema.asArraySchema();
            ArraySchema.Items items = arraySchema.getItems();
            if (items.isSingleItems()) {
                processSchema(jsonSchema, items.asSingleItems().getSchema(), spaces + "  ");
            } else {
                for (JsonSchema itemSchema : items.asArrayItems().getJsonSchemas()) {
                    processSchema(jsonSchema, itemSchema, spaces + "  ");
                }
            }
            jsonSchema.append("\n").append(spaces).append("}");
        } else if (schema.isStringSchema()) {
            jsonSchema.append(spaces).append("\"type\": \"string\"");
            if (schema.getId() != null && schema.getId().equals("color")) {
                jsonSchema.append(",\n").append(spaces).append("\"format\": \"color\"");
            }
        } else if (schema.isNumberSchema()) {
            jsonSchema.append(spaces).append("\"type\": \"number\"");
        } else if (schema.isIntegerSchema()) {
            jsonSchema.append(spaces).append("\"type\": \"integer\"");
        } else if (schema.isBooleanSchema()) {
            jsonSchema.append(spaces).append("\"type\": \"boolean\"");
        } else if (schema.isSimpleTypeSchema()) {
            jsonSchema.append(spaces).append("\"type\": \"").append(schema.asSimpleTypeSchema().getType().name().toLowerCase()).append("\"");
        }
    }

    private static boolean isPrimitiveType(String type) {
        return type.equals("string") || type.equals("number") || type.equals("integer") || type.equals("boolean") || type.equals("double");
    }

    public static void main(String[] args) throws Exception {
        // Define your settings
        Map<String, Map<String, Setting<?>>> settings = new HashMap<>();
        settings.put("Identity Settings", ConfigSection.getSettings(IdentitySettings.class));
        settings.put("Biome Generation Settings", ConfigSection.getSettings(BiomePlacementConfig.class));
        settings.put("Biome Terrain Settings", ConfigSection.getSettings(BiomeTerrainSettings.class));
        settings.put("Biome Visual Settings", ConfigSection.getSettings(BiomeVisualSettings.class));
        settings.put("Surface Settings", ConfigSection.getSettings(SurfaceSettings.class));
        settings.put("Mob Settings", ConfigSection.getSettings(MobSettings.class));
        settings.put("Biome Resource Settings", ConfigSection.getSettings(BiomeResourceSettings.class));
        settings.put("Biome Structure Settings", ConfigSection.getSettings(BiomeStructureSettings.class));


        String jsonSchema = generateJsonSchema(settings);

        // write to file
        File file = new File("biome-schema.json");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonSchema);
            System.out.println("JSON Schema written to file biome-schema.json");
        }

        settings = new HashMap<>();
        settings.put("BlockSettings", ConfigSection.getSettings(BlockSettings.class));
        settings.put("CarverSettings", ConfigSection.getSettings(CarverSettings.class));
        settings.put("DimensionSettings", ConfigSection.getSettings(DimensionSettings.class));
        settings.put("GameRuleSettings", ConfigSection.getSettings(GameRuleSettings.class));
        settings.put("GenerationSettings", ConfigSection.getSettings(GenerationSettings.class));
        settings.put("ImageSettings", ConfigSection.getSettings(ImageSettings.class));
        settings.put("PortalSettings", ConfigSection.getSettings(PortalSettings.class));
        settings.put("DimensionPresetInfo", ConfigSection.getSettings(DimensionPresetInfo.class));
        settings.put("ResourceSettings", ConfigSection.getSettings(ResourceSettings.class));
        settings.put("SpawnSettings", ConfigSection.getSettings(SpawnSettings.class));
        settings.put("StructureSettings", ConfigSection.getSettings(StructureSettings.class));
        settings.put("TerrainSettings", ConfigSection.getSettings(TerrainSettings.class));
        settings.put("VisualSettings", ConfigSection.getSettings(VisualSettings.class));

        jsonSchema = generateJsonSchema(settings);

        // write to file
        file = new File("preset-schema.json");
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonSchema);
            System.out.println("JSON Schema written to file preset-schema.json");
        }
    }
}
