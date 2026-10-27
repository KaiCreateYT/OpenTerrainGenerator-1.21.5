package com.pg85.otg.config.annotationprocessor;

import com.pg85.otg.config.annotation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.pg85.otg.config.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ConfigAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Config.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                processConfigClass(typeElement);
            }
        }
        return true;
    }

    private record SettingInfo(String fieldName, String name, String description, String className) {
        String getFieldGetter(boolean isBool) {
            return isBool ? "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "()"
                    : "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "()";
        }
        String getFieldGetter() {
            return getFieldGetter(false);
        }
        String getSettingName() {
            return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        }
    }

    private void processConfigClass(TypeElement classElement) {
        StringBuilder packageDeclaration = new StringBuilder();
        StringBuilder imports = new StringBuilder();
        StringBuilder classDeclaration = new StringBuilder();
        StringBuilder fields = new StringBuilder();
        String classbodyEnd = "}\n\n";

        StringBuilder output = new StringBuilder();

        String qualifiedName = String.valueOf(classElement.getQualifiedName());
        String configClassName = String.valueOf(classElement.getSimpleName());
        String className = String.valueOf(classElement.getSimpleName()).replace("Config", "Settings");

        classDeclaration.append("public class ")
                .append(className)
                .append(" extends ConfigSection")
                .append(" {\n\n");

        classDeclaration.append("\t@Override\n")
                .append("\tpublic String getSectionName() {\n")
                .append("\t\treturn \"")
                // Let's add spaces before capital letters after the first one
                .append(className.replaceAll("([a-z])([A-Z])", "$1 $2"))
                .append("\";\n")
                .append("\t}\n\n");

        packageDeclaration.append("package ")
                .append(qualifiedName, 0, qualifiedName.lastIndexOf('.'))
                .append(".generated")
                .append(";\n");

        imports.append("import ")
                .append("com.pg85.otg.config.settings.ConfigSection;\n");
        imports.append("import ")
                .append("com.pg85.otg.config.settingtype.Settings;\n");
        imports.append("import ")
                .append("com.pg85.otg.config.settingtype.Setting;\n");
        imports.append("import ")
                .append("com.pg85.otg.config.io.SettingsMap;\n");
        imports.append("import ")
                .append("java.util.List;\n");

        imports.append("import ")
                .append(qualifiedName)
                .append(";\n");

        List<SettingInfo> settings = handleFields(classElement, fields, imports);

        StringBuilder settingReader = addReader(configClassName, settings);

        StringBuilder settingsList = addSettingList(settings);

        output.append(packageDeclaration)
                .append("\n")
                .append(imports)
                .append("\n")
                .append(classDeclaration)
                .append("\n")
                .append(fields)
                .append("\n")
                .append(settingReader)
                .append("\n")
                .append(settingsList)
                .append(classbodyEnd);
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(className, classElement);
            new PrintWriter(sourceFile.openWriter()) {
                {
                    out.write(output.toString());
                    close();
                }
            };
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate config source file: " + e.getMessage());
        }
    }

    private StringBuilder addSettingList(List<SettingInfo> settings) {
        StringBuilder sb = new StringBuilder();

        sb.append("\tpublic static List <Setting<?>> list = List.of(");

        for (SettingInfo info : settings) {
            sb.append(info.getSettingName())
                    .append(", ");
        }

        // Remove the last comma and space
        if (sb.length() > 2)
            sb.setLength(sb.length() - 2);

        sb.append(");\n");

        return sb;
    }

    private StringBuilder addReader(String className, List<SettingInfo> settings) {
        StringBuilder reader = new StringBuilder();
        reader.append(String.format("\tpublic static %s.%sBuilder getBuilder(SettingsMap settingsMap) {\n",
                className, className));
        reader.append(String.format("\t\tvar builder = %s.builder();\n", className));
        for (SettingInfo info : settings) {
            reader.append("\t\tbuilder.")
                    .append(info.fieldName())
                    .append("(settingsMap.getSetting(")
                    .append(info.getSettingName())
                    .append("));\n");
        }
        reader.append("\t\treturn builder;\n")
                .append("\t}\n\n");
        return reader;
    }

    private static List<SettingInfo> handleFields(TypeElement classElement, StringBuilder fields, StringBuilder imports) {
        List<SettingInfo> settingInfos = new java.util.ArrayList<>();
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.FIELD) {
                continue;
            }
            Ignore ignore = enclosed.getAnnotation(Ignore.class);
            if (ignore != null) {
                // This is a parent/dependent/etc value - don't make a setting for it
                continue;
            }

            TypeMirror fieldType = enclosed.asType();

            fields.append("\t");

            String fieldName = enclosed.getSimpleName().toString();

            Description descriptionAnnotation = enclosed.getAnnotation(Description.class);
            LongDescription longDescriptionAnnotation = enclosed.getAnnotation(LongDescription.class);
            Name nameAnnotation = enclosed.getAnnotation(Name.class);

            String name =
                    nameAnnotation != null ? nameAnnotation.value()
                            : fieldName.transform(s -> s.substring(0, 1).toUpperCase()+ s.substring(1));
            String description =
                    descriptionAnnotation != null ? descriptionAnnotation.value()
                            : longDescriptionAnnotation != null ? String.join("\", \"", longDescriptionAnnotation.value())
                            : "";

            SettingInfo info = new SettingInfo(fieldName, name, description, String.valueOf(classElement.getSimpleName()));
            settingInfos.add(info);

            // Handle primitives
            if (fieldType.getKind().isPrimitive()) {
                handlePrimitives(enclosed, fieldType, fields, info);
            } else if (fieldType instanceof DeclaredType declaredType) {
                handleDeclaredTypes(enclosed, declaredType, imports, fields, info);
            } else {
                // Handle non-primitive, non-declared types (e.g., Object)
                throw new IllegalArgumentException("Unsupported variable type: " + fieldType);
            }
        }
        return settingInfos;
    }

    private static void handleDeclaredTypes(Element enclosed, DeclaredType declaredType, StringBuilder imports, StringBuilder fields, SettingInfo info) {
        Element type = declaredType.asElement();

        if (!type.toString().startsWith("java.lang.")) {
            imports.append("import ")
                    .append(type)
                    .append(";\n");
        }

        switch (type.getKind()) {
            case ENUM -> {
                // EnumSetting
                EnumSetting enumSetting = enclosed.getAnnotation(EnumSetting.class);
                String fieldTypeName = String.valueOf(type.getSimpleName());

                String defaultEnumValue = enumSetting != null ? enumSetting.value() : "DEFAULT";
                // <T extends Enum<T>> Setting<T> enumSetting(String name, T defaultValue, Function getter, String ...description)
                fields.append(String.format("public static final Setting<%s> %s = Settings.enumSetting(\"%s\", %s.%s, t -> ((%s) t).%s, \"%s\");%n",
                        fieldTypeName, info.getSettingName(), info.name, fieldTypeName, defaultEnumValue, info.className, info.getFieldGetter(), info.description));
            }
            case CLASS, RECORD, INTERFACE -> {

                // Handle String, Color, Other custom settings
                if (type.toString().equals("java.lang.String")) {
                    StringSetting stringSetting = enclosed.getAnnotation(StringSetting.class);
                    String defaultValue = stringSetting != null ? stringSetting.value() : StringSetting.DEFAULT_VALUE;
                    // Setting<String> stringSetting(String name, String defaultValue, Function getter, String ...description)
                    fields.append(String.format("public static final Setting<String> %s = Settings.stringSetting(\"%s\", \"%s\", t -> ((%s) t).%s, \"%s\");%n",
                            info.getSettingName(), info.name, defaultValue, info.className, info.getFieldGetter(), info.description));

                } else if (type.toString().equals("com.pg85.otg.util.Color")) {
                    ColorSetting colorSetting = enclosed.getAnnotation(ColorSetting.class);
                    String defaultValue = colorSetting != null ? colorSetting.value() : ColorSetting.DEFAULT_VALUE;
                    // Setting<Color> colorSetting(String name, String defaultValue, Function getter, String... description)
                    fields.append(String.format("public static final Setting<Color> %s = Settings.colorSetting(\"%s\", \"%s\", t -> ((%s) t).%s, \"%s\");%n",
                            info.getSettingName(), info.name, defaultValue, info.className, info.getFieldGetter(), info.description));

                } else if (type.toString().equals("java.util.List")) {
                    StringListSetting stringListSetting = enclosed.getAnnotation(StringListSetting.class);
                    String[] defaultValue = stringListSetting != null ? stringListSetting.value() : StringListSetting.DEFAULT_VALUE;
                    // Setting<List<String>> stringListSetting(String name, String[] defaultValues, Function<ConfigSection, List<String>> getter, String ...description)

                    String defaultValueString = String.join("\", \"", defaultValue);

                    if (!defaultValueString.isEmpty()) {
                        defaultValueString = "\""+defaultValueString+"\"";
                    }

                    fields.append(String.format("public static final Setting<List<String>> %s = Settings.stringListSetting(\"%s\", new String[] {%s}, t -> ((%s) t).%s, \"%s\");%n",
                            info.getSettingName(), info.name, defaultValueString, info.className, info.getFieldGetter(), info.description));

                } else {
                    // Handle other custom types or throw an error
                    throw new IllegalArgumentException("Unsupported class: " + type);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private static void handlePrimitives(Element enclosed, TypeMirror fieldType, StringBuilder fields, SettingInfo info) {
        switch (fieldType.getKind()) {
            case BOOLEAN -> {
                BooleanSetting booleanSetting = enclosed.getAnnotation(BooleanSetting.class);
                boolean defaultValue = booleanSetting != null ? booleanSetting.value() : BooleanSetting.DEFAULT_VALUE;
                // Setting<Boolean> booleanSetting(String name, boolean defaultValue, Function getter, String... description)
                fields.append(String.format("public static final Setting<Boolean> %s = Settings.booleanSetting(\"%s\", %s, t -> ((%s) t).%s, \"%s\");%n",
                        info.getSettingName(), info.name, defaultValue, info.className, info.getFieldGetter(true), info.description));
            }
            case INT -> {
                IntSetting intSetting = enclosed.getAnnotation(IntSetting.class);
                int defaultValue = intSetting != null ? intSetting.value() : IntSetting.DEFAULT_VALUE;
                int minValue = intSetting != null ? intSetting.min() : IntSetting.DEFAULT_MIN;
                int maxValue = intSetting != null ? intSetting.max() : IntSetting.DEFAULT_MAX;
                // Setting<Integer> intSetting(String name, int defaultValue, int min, int max, Function getter, String... description)
                fields.append(String.format("public static final Setting<Integer> %s = Settings.intSetting(\"%s\", %d, %d, %d, t -> ((%s) t).%s, \"%s\");%n",
                        info.getSettingName(), info.name, defaultValue, minValue, maxValue, info.className, info.getFieldGetter(), info.description));
            }
            case DOUBLE -> {
                DoubleSetting doubleSetting = enclosed.getAnnotation(DoubleSetting.class);
                double defaultValue = doubleSetting != null ? doubleSetting.value() : DoubleSetting.DEFAULT_VALUE;
                double minValue = doubleSetting != null ? doubleSetting.min() : DoubleSetting.DEFAULT_MIN;
                double maxValue = doubleSetting != null ? doubleSetting.max() : DoubleSetting.DEFAULT_MAX;
                // Setting<Double> doubleSetting(String name, double defaultValue, double min, double max, Function getter, String... description)
                fields.append(String.format("public static final Setting<Double> %s = Settings.doubleSetting(\"%s\", %f, %f, %f, t -> ((%s) t).%s, \"%s\");%n",
                        info.getSettingName(), info.name, defaultValue, minValue, maxValue, info.className, info.getFieldGetter(), info.description));
            }
            case FLOAT -> {
                FloatSetting floatSetting = enclosed.getAnnotation(FloatSetting.class);
                float defaultValue = floatSetting != null ? floatSetting.value() : FloatSetting.DEFAULT_VALUE;
                float minValue = floatSetting != null ? floatSetting.min() : FloatSetting.DEFAULT_MIN;
                float maxValue = floatSetting != null ? floatSetting.max() : FloatSetting.DEFAULT_MAX;
                // Setting<Float> floatSetting(String name, float defaultValue, float min, float max, Function getter, String... description)
                fields.append(String.format("public static final Setting<Float> %s = Settings.floatSetting(\"%s\", %f, %f, %f, t -> ((%s) t).%s, \"%s\");%n",
                        info.getSettingName(), info.name, defaultValue, minValue, maxValue, info.className, info.getFieldGetter(), info.description));
            }
            case LONG -> {
                LongSetting longSetting = enclosed.getAnnotation(LongSetting.class);
                long defaultValue = longSetting != null ? longSetting.value() : LongSetting.DEFAULT_VALUE;
                long minValue = longSetting != null ? longSetting.min() : LongSetting.DEFAULT_MIN;
                long maxValue = longSetting != null ? longSetting.max() : LongSetting.DEFAULT_MAX;
                // Setting<Long> longSetting(String name, long defaultValue, long min, long max, Function getter, String... description)
                fields.append(String.format("public static final Setting<Long> %s = Settings.longSetting(\"%s\", %d, %d, %d, t -> ((%s) t).%s, \"%s\");%n",
                        info.getSettingName(), info.name, defaultValue, minValue, maxValue, info.className, info.getFieldGetter(), info.description));
            }
            default -> throw new IllegalArgumentException("Unsupported primitive type: " + fieldType);
        }
    }
}
