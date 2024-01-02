
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package general;

import com.artformgames.injector.bungeeauthproxy.BungeeAuthProxyInjector;
import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.packet.*;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class InitialHandler extends net.md_5.bungee.connection.InitialHandler implements PendingConnection {
    private final BungeeCord bungee;
    private ChannelWrapper ch;
    private final ListenerInfo listener;
    private Handshake handshake;
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    private PluginMessage brandMessage;
    private final Set<String> registeredChannels = new HashSet<>();
    private State thisState;
    private final Connection.Unsafe unsafe;
    private boolean onlineMode;
    private InetSocketAddress virtualHost;
    private String name;
    private UUID uniqueId;
    private UUID offlineId;
    private LoginResult loginProfile;
    private boolean legacy;
    private String extraDataInHandshake;

    public InitialHandler(BungeeCord bungee, ListenerInfo listener, BungeeCord bungee1, ListenerInfo listener1, Unsafe unsafe) {
        super(bungee, listener);
        this.bungee = bungee1;
        this.listener = listener1;
        this.unsafe = unsafe;
    }

    public void handle(EncryptionResponse encryptResponse) throws Exception {
        Preconditions.checkState(this.thisState == InitialHandler.State.ENCRYPT, "Not expecting ENCRYPT");
        SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, this.request);
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        this.ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        this.ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
        String encName = URLEncoder.encode(this.getName(), "UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[][] data = new byte[][]{this.request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};
        for (byte[] datum : data) {
            sha.update(datum);
        }

        String encodedHash = URLEncoder.encode((new BigInteger(sha.digest())).toString(16), "UTF-8");
        this.thisState = InitialHandler.State.FINISHING;
        BungeeAuthProxyInjector.submitRequest(this, encodedHash, this.ch.getHandle().eventLoop(), (result, error) -> {
            if (error == null) {
                LoginResult obj = BungeeCord.getInstance().gson.fromJson(result, LoginResult.class);
                if (obj != null && obj.getId() != null) {
                    InitialHandler.this.loginProfile = obj;
                    InitialHandler.this.name = obj.getName();
                    InitialHandler.this.uniqueId = Util.getUUID(obj.getId());
                    InitialHandler.this.finish();
                    return;
                }
                InitialHandler.this.disconnect(InitialHandler.this.bungee.getTranslation("offline_mode_player", new Object[0]));
            } else {
                InitialHandler.this.disconnect(InitialHandler.this.bungee.getTranslation("mojang_fail", new Object[0]));
                InitialHandler.this.bungee.getLogger().log(Level.SEVERE, "Error authenticating " + InitialHandler.this.getName() + " with minecraft.net", error);
            }
        });
    }

    public void finish() {

    }

    public ListenerInfo getListener() {
        return this.listener;
    }

    public Handshake getHandshake() {
        return this.handshake;
    }

    public LoginRequest getLoginRequest() {
        return this.loginRequest;
    }

    public PluginMessage getBrandMessage() {
        return this.brandMessage;
    }

    public Set<String> getRegisteredChannels() {
        return this.registeredChannels;
    }

    public boolean isOnlineMode() {
        return this.onlineMode;
    }

    public InetSocketAddress getVirtualHost() {
        return this.virtualHost;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public UUID getOfflineId() {
        return this.offlineId;
    }

    public LoginResult getLoginProfile() {
        return this.loginProfile;
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public String getExtraDataInHandshake() {
        return this.extraDataInHandshake;
    }

    private enum State {
        HANDSHAKE,
        STATUS,
        PING,
        USERNAME,
        ENCRYPT,
        FINISHING;

        State() {
        }
    }
}
