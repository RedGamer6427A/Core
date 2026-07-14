package dev.redgamer6427a.core.messagebus.server;

import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.Message;
import dev.redgamer6427a.core.utils.CidrRange;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BrokerThread implements Runnable {
    @Getter @Setter
    private List<CidrRange> cidrRanges;
    @Setter
    private String pass;
    @Getter @Setter
    private int port;
    @Getter
    private boolean isRunning;

    @Getter
    private List<BrokerProcessor> brokerProcessors = new ArrayList<>();




    @Getter
    private Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    @Getter
    private final LinkedBlockingDeque<Message> messages = new LinkedBlockingDeque<>();

    private SSLServerSocket serverSocket;
    private final ExecutorService clientPool = Executors.newVirtualThreadPerTaskExecutor();

    private static final Logger logger = Logger.create();

    public BrokerThread(int port, String pass) {
        this(port, pass, new ArrayList<>());
    }

    public BrokerThread(int port, String pass, List<CidrRange> cidrRanges) {
        this.pass = pass;
        this.port = port;

        this.cidrRanges = cidrRanges;
        this.isRunning = false;
    }

    public void stop() {
        if (!isRunning) {
            logger.warning("Broker thread isn't running!");
            return;
        }
        isRunning = false;
        try {
            if (serverSocket != null) serverSocket.close(); // unblocks accept()
        } catch (IOException e) {
            logger.catching("Error closing broker socket", e);
        }
        clientPool.shutdown();
    }

    private final Map<String, FailTracker> failedAttempts = new ConcurrentHashMap<>();

    private record FailTracker(AtomicInteger count, AtomicLong lastAttempt) {}

    public void recordAuthFailure(String ip) {
        FailTracker tracker = failedAttempts.computeIfAbsent(ip,
                k -> new FailTracker(new AtomicInteger(0), new AtomicLong(0)));
        tracker.count().incrementAndGet();
        tracker.lastAttempt().set(System.currentTimeMillis());
    }

    public boolean isRateLimited(String ip) {
        FailTracker tracker = failedAttempts.get(ip);
        if (tracker == null) return false;

        int failures = tracker.count().get();
        if (failures < 5) return false;

        long elapsed = System.currentTimeMillis() - tracker.lastAttempt().get();
        long requiredCooldown = Math.min(1000L * (1L << (failures - 5)), 60_000); // exponential, cap 60s
        return elapsed < requiredCooldown;
    }

    public void unregisterClient(ClientConnection connection) {
        if (connection.getClientId() != null) {
            connections.remove(connection.getClientId());
        }
    }

    public ClientConnection registerClient(ClientConnection connection) {
        if (connection.getClientId() == null) {
            logger.throwing(new IllegalArgumentException("Cannot register a client without a client id!"));
            return null;
        }

        return connections.putIfAbsent(connection.getClientId(), connection);
    }



    @Override
    public void run() {
        if (isRunning) {
            logger.warning("Broker thread is already running!");
            return;
        }

        try {
            SSLContext ctx = createSslContext();
            SSLServerSocketFactory factory = ctx.getServerSocketFactory();
            serverSocket = (SSLServerSocket) factory.createServerSocket(port, 50);
            isRunning = true;
            logger.info("Broker listening on port " + port);
            startDispatcher();
            while (isRunning) {
                SSLSocket clientSocket;
                try {
                    clientSocket = (SSLSocket) serverSocket.accept(); // blocks til connect or socket closed

                } catch (IOException e) {
                    if (!isRunning) break; // expected — stop() closed the socket
                    logger.catching("Accept failed", e);
                    continue;
                }

                if (!isAllowed(clientSocket.getInetAddress())) {
                    logger.warning("Rejected connection from " + clientSocket.getInetAddress());
                    clientSocket.close();
                    continue;
                }
                if (isRateLimited(clientSocket.getInetAddress().getHostAddress())) {
                    clientSocket.close(); // bai hecker
                    continue;
                }
                clientPool.submit(new ClientConnection(clientSocket, this, pass));
            }
        } catch (IOException | GeneralSecurityException e) {
            logger.catching("Broker accept loop failed", e);
        } finally {
            isRunning = false;
        }
    }

    private boolean isAllowed(InetAddress addr) {
        if (cidrRanges.isEmpty()) return true; // no ranges configured = allow all
        return cidrRanges.stream().anyMatch(r -> r.contains(addr));
    }

    private SSLContext createSslContext() throws GeneralSecurityException, IOException {

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        X500Name subject = new X500Name("CN=message-hub");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000);

        ContentSigner signer;
        try {
            signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC")
                    .build(privateKey);
        } catch (OperatorCreationException e) {
            // wrap into a checked type your method already declares, keep root cause attached
            logger.catching(e);
            throw new GeneralSecurityException("Failed to create content signer for self-signed cert", e);
        }

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, keyPair.getPublic()
        );
        X509Certificate cert = new JcaX509CertificateConverter()
                .getCertificate(certBuilder.build(signer));

        // password for the in-memory keystore entry (doesn't need to be memorable, just non-null)
        char[] password = "unused".toCharArray();

        // build keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("hubcert", privateKey, password, new Certificate[]{cert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx;
    }

    private void startDispatcher() {
        Thread dispatcher = new Thread(() -> {
            while (isRunning) {
                try {
                    Message msg = messages.take(); // blocks until available
                    dispatch(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "broker-dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    public void dispatch(Message msg) {

        for (BrokerProcessor processor : brokerProcessors) {
            processor.getConsumer().accept(msg);
        }

        List<ClientConnection> targets = connections.entrySet().stream()
                .filter(e -> matchesPattern(msg.destination(), e.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        if (targets.isEmpty()) {
            logger.warning("No subscriber for destination: " + msg.destination());
            return;
        }
        for (ClientConnection target : targets) {
            try {
                target.pushMessage(msg);
            } catch (IOException e) {
                logger.catching("Failed to push message to " + target.getClientId(), e);
            }
        }
    }

    private boolean matchesPattern(String pattern, String destination) {
        if (Objects.equals(pattern, "*")) return true;
        if (!pattern.contains("*")) return pattern.equals(destination);
        String regex = Arrays.stream(pattern.split("\\*", -1))
                .map(java.util.regex.Pattern::quote)
                .collect(Collectors.joining(".*"));
        return destination.matches(regex);
    }

}