package com.artformgames.injector.bungeeauthproxy.transformer;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static com.artformgames.injector.bungeeauthproxy.Logging.debug;
import static com.artformgames.injector.bungeeauthproxy.Logging.error;

public class ProxyHandlerTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!className.equals("net/md_5/bungee/http/HttpClient")) return classFileBuffer;
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new LoaderClassPath(loader));
            CtClass clazz = pool.makeClass(new ByteArrayInputStream(classFileBuffer));
            CtClass callbackClass = pool.getCtClass("net.md_5.bungee.api.Callback");
            CtClass eventLoopClass = pool.getCtClass("io.netty.channel.EventLoop");
            CtClass stringClass = pool.getCtClass("java.lang.String");

            CtMethod handleMethod = clazz.getDeclaredMethod("get", new CtClass[]{stringClass, eventLoopClass, callbackClass});
            debug("Injecting into " + handleMethod.getLongName());

            handleMethod.setBody("{com.artformgames.injector.bungeeauthproxy.BungeeAuthProxy.submitRequest($1, $2, $3);}");

            // remove unused static initializer
            CtConstructor staticBlock = clazz.getClassInitializer();
            if (staticBlock != null) clazz.removeConstructor(staticBlock);

            // remove unused cache field
            CtField cacheField = clazz.getField("addressCache");
            clazz.removeField(cacheField);


            return clazz.toBytecode();
        } catch (Exception e) {
            error("Failed to inject handlers into [" + className + "], are you really using BungeeCord?", e);
            return classFileBuffer;
        }
    }

}