package com.test.webtest.domain.securityvitals.scan;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;

/**
 * JDK 표준 SSL 핸드셰이크로 TLS 정보를 점검하는 구현체
 * - 체인 유효(TrustStore 기준) 여부
 * - 만료까지 남은 일수, Issuer, Subject 추출
 * - SNI 설정 포함
 */

public class JdkTlsInspector implements SslInspector {

    @Override
    public Result inspect(String host, int port) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // default trust store
            ctx.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory factory = ctx.getSocketFactory();
            try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
                // SNI
                SSLParameters params = socket.getSSLParameters();
                params.setServerNames(java.util.List.of(new SNIHostName(host)));
                socket.setSSLParameters(params);

                socket.setSoTimeout(10_000);
                socket.startHandshake();

                SSLSession session = socket.getSession();
                X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];

                boolean chainValid = true; // 핸드셰이크 성공 == 체인 유효
                boolean valid = true;

                Instant notAfter = cert.getNotAfter().toInstant();
                int daysRemaining = (int) Duration.between(Instant.now(), notAfter).toDays();

                String issuer = cert.getIssuerX500Principal().getName();
                String subject = cert.getSubjectX500Principal().getName();

                return new Result(valid, chainValid, daysRemaining, issuer, subject);
            }
        } catch (Exception e) {
            return new Result(false, false, null, null, null);
        }
    }
}
