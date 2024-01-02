package com.artformgames.injector.bungeeauthproxy.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


public class ProxyHandlerTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!className.equals("net/md_5/bungee/connection/InitialHandler")) return classFileBuffer;
        
        System.out.println("Handling: " + className);

        byte[] transformed = null;
        ClassPool pool = ClassPool.getDefault();
        CtClass cl = null;

        try {
            cl = pool.makeClass(new ByteArrayInputStream(classFileBuffer));
            CtClass responseClass = pool.getCtClass("net.md_5.bungee.protocol.packet.EncryptionResponse");

            CtMethod handleMethod = cl.getDeclaredMethod("handle", new CtClass[]{responseClass});
            handleMethod.insertBefore("System.out.println(\"Successfully loaded!!!!!\");");

            transformed = cl.toBytecode();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }

        return transformed;
    }

}