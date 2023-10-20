package br.dev.brunoxkk0.syrxmccore.core.config;

import br.dev.brunoxkk0.syrxmccore.helper.ReflectionUtils;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Logger;

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

                fileConfig.set("file_version", obj.getFileVersion());
                fileConfig.setComment("file_version", "This field is used to manage the config version. !DON'T MODIFY!");

                syncComments(fileConfig, config);
                fileConfig.save();

                return obj;

            }

            if (!fileConfig.contains("file_version") || fileConfig.getLong("file_version") != obj.getFileVersion()) {

                for (Field field : obj.getClass().getFields()) {

                    if (!field.isAnnotationPresent(Path.class))
                        continue;

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

                fileConfig.set("file_version", obj.getFileVersion());

                syncComments(fileConfig, config);

                fileConfig.save();
                fileConfig.load();

                logger.info("The file " + file.getPath() + " is outdated. The file was updated with the default values for the new fields.");
            }

            objectConverter.toObject(fileConfig, obj);

            return obj;

        }

    }

    private static void syncComments(CommentedFileConfig config, Class<?> source) {

        for (Field field : source.getFields()) {

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

        public abstract long getFileVersion();

    }

}
