package br.dev.brunoxkk0.syrxmccore.core.config;

import br.dev.brunoxkk0.syrxmccore.helper.ReflectionUtils;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConfigFactory {

    private static final Logger logger = Logger.getLogger("ConfigFactory");

    @SneakyThrows
    public static <T extends PathConfig> T loadConfig(java.nio.file.Path path, Class<T> config) {

        File file = path.toFile();

        if (!file.getParentFile().exists()) {
            if (file.getParentFile().mkdirs())
                logger.info("Created folder at " + file.getParentFile().getPath());
        }

        if (!file.exists()) {
            if (file.createNewFile())
                logger.info("Created config file at " + file.getPath());
        }

        ObjectConverter objectConverter = new ObjectConverter();

        try (CommentedFileConfig fileConfig = CommentedFileConfig.ofConcurrent(file)) {

            fileConfig.load();

            T obj = ReflectionUtils.createInstance(config);

            ((PathConfig) obj).configPath = path;
            ((PathConfig) obj).config = fileConfig;

            if (fileConfig.isEmpty()) {

                objectConverter.toConfig(obj, fileConfig);
                syncComments(fileConfig, config);
                fileConfig.save();

                return obj;

            } else {

                for (Field field : Arrays.stream(obj.getClass().getDeclaredFields()).filter(field ->
                        field.isAnnotationPresent(Path.class)
                ).collect(Collectors.toList())) {

                    Path pt = field.getAnnotation(Path.class);

                    try {
                        if (fileConfig.get(pt.value()) == null && field.get(obj) != null) {
                            fileConfig.set(pt.value(), field.get(obj));
                        }
                    } catch (Exception exception) {
                        logger.warning("An error occurs when updated the config " + file.getPath() +
                                " { Field: " + field.getName() + ", Path: " + pt.value() + " } Message: " +
                                exception.getMessage()
                        );
                    }

                }

                syncComments(fileConfig, config);

                fileConfig.save();
                fileConfig.load();

                logger.info("The file " + file.getPath() + " is a valid configuration, but is outdated, config was updated with the default values.");
            }

            objectConverter.toObject(fileConfig, obj);

            return obj;

        }

    }

    private static void syncComments(CommentedFileConfig config, Class<?> source) {

        for (Field field : source.getDeclaredFields()) {
            if (field.isAnnotationPresent(Path.class) && field.isAnnotationPresent(Comment.class)) {

                Path path = field.getAnnotation(Path.class);
                Comment comment = field.getAnnotation(Comment.class);

                if (!comment.value().isEmpty()) {
                    config.setComment(path.value(), comment.value());
                }

            }
        }

    }


    @Getter
    public static abstract class PathConfig {

        private transient java.nio.file.Path configPath;
        private transient CommentedFileConfig config;

    }


}
