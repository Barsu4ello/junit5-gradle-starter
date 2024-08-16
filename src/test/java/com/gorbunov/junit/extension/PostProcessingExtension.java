package com.gorbunov.junit.extension;

import com.gorbunov.junit.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;

public class PostProcessingExtension implements TestInstancePostProcessor {

    // Так работает Spring
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        System.out.println("Post processing extension");
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if(field.isAnnotationPresent(Test.class)) { // но нужно естественно, чтобы была аннотация типа @Autowired или типо того и проверка на тип поля.
                field.set(testInstance, new UserService(null));
            }
        }
    }
}
