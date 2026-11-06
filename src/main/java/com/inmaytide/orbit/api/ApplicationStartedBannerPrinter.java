package com.inmaytide.orbit.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.Enumeration;

@Component
public class ApplicationStartedBannerPrinter implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartedBannerPrinter.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String port;

    private final Environment environment;

    public ApplicationStartedBannerPrinter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Duration duration = event.getTimeTaken();
        log.info("\n----------------------------------------------------------------------------" +
                        "\nApplication '{}' started successfully!   🚀🚀🚀" +
                        "\nStarted in:        {} seconds" +
                        "\nProfile(s):        {}" +
                        "\nReady to accept requests   📡📡📡" +
                        "\nAccess URLs:" +
                        "\n    Local:         {}" +
                        "\n    External:      {}" +
                        "\n----------------------------------------------------------------------------"
                ,
                applicationName,
                duration == null ? "-" : BigDecimal.valueOf(duration.toMillis()).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP).stripTrailingZeros(),
                environment.getActiveProfiles(),
                buildAccessURL("127.0.0.1"),
                buildAccessURL(getLocalIpAddress())
        );
    }

    private String buildAccessURL(String ipAddress) {
        if ("UNKNOWN".equalsIgnoreCase(ipAddress)) {
            return "UNKNOWN";
        }
        String protocol = "http";
        if (environment.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        return String.format("%s://%s:%s/doc.html", protocol, ipAddress, port);
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        if (isLanIpAddress(addr.getHostAddress())) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Could not get local host address", e);
        }
        return "UNKNOWN";
    }

    private static boolean isLanIpAddress(String ip) {
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
    }
}
