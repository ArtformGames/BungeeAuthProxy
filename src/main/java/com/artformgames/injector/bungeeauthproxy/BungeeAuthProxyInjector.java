package com.artformgames.injector.bungeeauthproxy;

import cc.carm.lib.configuration.EasyConfiguration;
import cc.carm.lib.configuration.core.source.ConfigurationProvider;
import com.artformgames.injector.bungeeauthproxy.conf.Config;
import com.artformgames.injector.bungeeauthproxy.handler.ProxiedAuthHandler;
import com.artformgames.injector.bungeeauthproxy.util.Logging;
import io.netty.channel.EventLoop;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.connection.InitialHandler;

import java.lang.instrument.Instrumentation;

public class BungeeAuthProxyInjector {

    private static Instrumentation instance = null;
    protected static ProxiedAuthHandler handler;
    protected static ConfigurationProvider<?> configurationProvider;

    public static void premain(String args, Instrumentation instrumentation) {
        instance = instrumentation;
        Logging.log(Logging.Level.INFO, "Loading auth configurations...");
        configurationProvider = EasyConfiguration.from("auth.yml");
        configurationProvider.initialize(Config.class);

        Logging.log(Logging.Level.INFO, "Initializing auth handler...");
        handler = new ProxiedAuthHandler();

        Logging.log(Logging.Level.INFO, "Injecting InitialHandler...");
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.importPackage("com.artformgames.injector.bungeeauthproxy");

            CtClass handlerClass = pool.getCtClass("net.md_5.bungee.connection.InitialHandler");
            CtClass responseClass = pool.getCtClass("net.md_5.bungee.protocol.packet.EncryptionResponse");

            CtMethod handleMethod = handlerClass.getDeclaredMethod("handle", new CtClass[]{responseClass});
            Logging.log(Logging.Level.DEBUG, "Found target method: " + handleMethod.getLongName());

            handleMethod.setBody("{\n" +
                    "        Preconditions.checkState(this.thisState == InitialHandler.State.ENCRYPT, \"Not expecting ENCRYPT\");\n" +
                    "        SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, this.request);\n" +
                    "        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);\n" +
                    "        this.ch.addBefore(\"frame-decoder\", \"decrypt\", new CipherDecoder(decrypt));\n" +
                    "        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);\n" +
                    "        this.ch.addBefore(\"frame-prepender\", \"encrypt\", new CipherEncoder(encrypt));\n" +
                    "        String encName = URLEncoder.encode(this.getName(), \"UTF-8\");\n" +
                    "        MessageDigest sha = MessageDigest.getInstance(\"SHA-1\");\n" +
                    "        byte[][] data = new byte[][]{this.request.getServerId().getBytes(\"ISO_8859_1\"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};\n" +
                    "        for (byte[] datum : data) {\n" +
                    "            sha.update(datum);\n" +
                    "        }\n" +
                    "\n" +
                    "        String encodedHash = URLEncoder.encode((new BigInteger(sha.digest())).toString(16), \"UTF-8\");\n" +
                    "        this.thisState = InitialHandler.State.FINISHING;\n" +
                    "        BungeeAuthProxyInjector.submitRequest(this, encodedHash, this.ch.getHandle().eventLoop(), (result, error) -> {\n" +
                    "            if (error == null) {\n" +
                    "                LoginResult obj = (LoginResult) BungeeCord.getInstance().gson.fromJson(result, LoginResult.class);\n" +
                    "                if (obj != null && obj.getId() != null) {\n" +
                    "                    InitialHandler.this.loginProfile = obj;\n" +
                    "                    InitialHandler.this.name = obj.getName();\n" +
                    "                    InitialHandler.this.uniqueId = Util.getUUID(obj.getId());\n" +
                    "                    InitialHandler.this.finish();\n" +
                    "                    return;\n" +
                    "                }\n" +
                    "                InitialHandler.this.disconnect(InitialHandler.this.bungee.getTranslation(\"offline_mode_player\", new Object[0]));\n" +
                    "            } else {\n" +
                    "                InitialHandler.this.disconnect(InitialHandler.this.bungee.getTranslation(\"mojang_fail\", new Object[0]));\n" +
                    "                InitialHandler.this.bungee.getLogger().log(Level.SEVERE, \"Error authenticating \" + InitialHandler.this.getName() + \" with minecraft.net\", error);\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }");
            handlerClass.writeFile();
        } catch (Exception ex) {
            Logging.log(Logging.Level.ERROR, "Failed to inject handlers, are you really using BungeeCord?");
            ex.printStackTrace();
        }

    }

    public static void submitRequest(InitialHandler handler, String encodedHash,
                                     EventLoop loop, Callback<String> callback) throws Exception {
        Logging.log(Logging.Level.DEBUG, "Submitting request [" + handler.getName() + "] " + encodedHash);
        getHandler().submit(handler, encodedHash, loop, callback);
    }

    public static Instrumentation getInstance() {
        return instance;
    }

    public static ProxiedAuthHandler getHandler() {
        return handler;
    }


}
