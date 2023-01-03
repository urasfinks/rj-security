package ru.jamsys.component;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import ru.jamsys.App;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTest {

    @BeforeAll
    static void beforeAll() {
        String[] args = new String[]{};
        App.context = SpringApplication.run(App.class, args);
    }

    @Test
    void get() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        Security security = App.context.getBean(Security.class);
        security.init("12345".toCharArray());
        security.add("test", "12345".toCharArray());
        Assertions.assertEquals("12345", new String(security.get("test")), "#1");
        security.add("test", "123456".toCharArray());
        Assertions.assertEquals("123456", new String(security.get("test")), "#2");
        security.remove("test");
        assertNull(security.get("test"), "#3");
    }
}