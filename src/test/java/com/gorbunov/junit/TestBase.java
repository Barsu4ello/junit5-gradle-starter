package com.gorbunov.junit;

import com.gorbunov.junit.extension.GlobalExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({
        GlobalExtension.class
})
public abstract class TestBase {
}
