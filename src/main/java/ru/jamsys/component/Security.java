package ru.jamsys.component;

import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.jamsys.App;
import ru.jamsys.FileWriteOptions;
import ru.jamsys.UtilFile;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Security {

    @Setter
    private String path = "security/security.jks";

    private volatile KeyStore keyStore = null;
    private char[] password;
    AtomicBoolean isInit = new AtomicBoolean(false);

    public void init(char[] password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        if (isInit.compareAndSet(false, true)) {
            this.password = password;

            File f = new File(path);
            if (!f.exists()) {
                createKeyStore();
            } else {
                try (InputStream stream = new ByteArrayInputStream(UtilFile.readBytes(path))) {
                    keyStore = KeyStore.getInstance("JCEKS");
                    keyStore.load(stream, this.password);
                } catch (Exception e) {
                    keyStore = null;
                    e.printStackTrace();
                }
            }
        }
    }

    private void createKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, password);
        save();
    }

    public void add(String key, char[] value) {
        if (keyStore != null) {
            try {
                KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(password);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
                SecretKey generatedSecret = factory.generateSecret(new PBEKeySpec(value, "any".getBytes(), 13));
                keyStore.setEntry(key, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePP);
                save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Exception("Security компонент не инициализирован, исполните init(${pass})").printStackTrace();
        }
    }

    public char[] get(String key) {
        if (keyStore != null) {
            try {
                KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(password);
                KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) keyStore.getEntry(key, keyStorePP);
                if (ske != null) {
                    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
                    PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
                    return keySpec.getPassword();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Exception("Security компонент не инициализирован, исполните init(${pass})").printStackTrace();
        }
        return null;
    }

    public void remove(String key) {
        try {
            keyStore.deleteEntry(key);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        keyStore.store(byteArrayOutputStream, password);
        UtilFile.writeBytes(path, byteArrayOutputStream.toByteArray(), FileWriteOptions.CREATE_OR_REPLACE);
    }

}
