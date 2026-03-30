package be.teletask.onvif.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Digest (RFC 2617) kiegészítés a {@link HttpClient}-hez: egy 401 után
 * egyszer újrapróbálja a kérést Authorization fejléccel.
 */
public final class HttpDigest {

    private static final Pattern DIGEST_PARAM = Pattern.compile("(\\w+)=((?:\"([^\"]*)\")|([^,]+))");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ConcurrentMap<String, DigestState> DIGEST_STATE = new ConcurrentHashMap<>();

    private HttpDigest() {
    }

    private static final class DigestState {
        final String nonce;
        final String cnonce;
        final AtomicInteger nc;

        DigestState(String nonce, String cnonce, int startNc) {
            this.nonce = nonce;
            this.cnonce = cnonce;
            this.nc = new AtomicInteger(startNc);
        }
    }

    public static HttpResponse<String> execute(
            HttpClient client,
            String method,
            URI uri,
            Map<String, String> headers,
            String body,
            String username,
            String password,
            Duration timeout) throws IOException, InterruptedException {

        HttpRequest request = buildRequest(method, uri, headers, body, timeout, null);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 401 || username == null || username.isEmpty()) {
            return response;
        }

        String safePassword = password == null ? "" : password;
        // A többlépcsős (3x) retry ideiglenesen kikapcsolva.
        // Egyszer próbálkozunk hitelesített kéréssel a 401 challenge alapján.
        String authorization = buildAuthorizationFromChallenge(
                method,
                uri,
                username,
                safePassword,
                response.headers()
        );
        if (authorization == null) {
            return response;
        }
        HttpRequest retry = buildRequest(method, uri, headers, body, timeout, authorization);
        response = client.send(retry, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    /**
     * Ugyanaz, mint {@link #execute}, de a válasz törzse nyers bájtok (pl. snapshot kép).
     */
    public static HttpResponse<byte[]> executeBytes(
            HttpClient client,
            String method,
            URI uri,
            Map<String, String> headers,
            String body,
            String username,
            String password,
            Duration timeout) throws IOException, InterruptedException {

        HttpRequest request = buildRequest(method, uri, headers, body, timeout, null);
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 401 || username == null || username.isEmpty()) {
            return response;
        }

        String safePassword = password == null ? "" : password;
        // A többlépcsős (3x) retry ideiglenesen kikapcsolva.
        // Egyszer próbálkozunk hitelesített kéréssel a 401 challenge alapján.
        String authorization = buildAuthorizationFromChallenge(
                method,
                uri,
                username,
                safePassword,
                response.headers()
        );
        if (authorization == null) {
            return response;
        }
        HttpRequest retry = buildRequest(method, uri, headers, body, timeout, authorization);
        response = client.send(retry, HttpResponse.BodyHandlers.ofByteArray());
        return response;
    }

    private static HttpRequest buildRequest(
            String method,
            URI uri,
            Map<String, String> headers,
            String body,
            Duration timeout,
            String authorizationDigest) {

        HttpRequest.Builder b = HttpRequest.newBuilder(uri).timeout(timeout);
        if (headers != null) {
            headers.forEach(b::header);
        }
        if (authorizationDigest != null) {
            b.header("Authorization", authorizationDigest);
        }
        String m = method.toUpperCase(Locale.ROOT);
        if ("GET".equals(m)) {
            b.GET();
        } else if ("POST".equals(m)) {
            String payload = body != null ? body : "";
            b.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        return b.build();
    }

    private static String buildAuthorizationFromChallenge(
            String method,
            URI uri,
            String username,
            String password,
            java.net.http.HttpHeaders headers) {
        String digestChallenge = findAuthChallenge(headers, "Digest");
        if (digestChallenge != null) {
            Map<String, String> params = parseDigestChallenge(digestChallenge);
            if (!params.isEmpty()) {
                String digestAuthorization = buildAuthorizationHeader(method, uri, username, password, params);
                if (digestAuthorization != null) {
                    return digestAuthorization;
                }
            }
        }

        String basicChallenge = findAuthChallenge(headers, "Basic");
        if (basicChallenge != null) {
            String credentials = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            return "Basic " + encoded;
        }

        return null;
    }

    private static String findAuthChallenge(java.net.http.HttpHeaders headers, String scheme) {
        List<String> values = headers.allValues("WWW-Authenticate");
        for (String v : values) {
            if (v != null && v.trim().regionMatches(true, 0, scheme, 0, scheme.length())) {
                return v;
            }
        }
        return null;
    }

    static Map<String, String> parseDigestChallenge(String wwwAuthenticate) {
        Map<String, String> map = new HashMap<>();
        int i = wwwAuthenticate.toLowerCase(Locale.ROOT).indexOf("digest");
        if (i < 0) {
            return map;
        }
        String rest = wwwAuthenticate.substring(i + "digest".length()).trim();
        Matcher m = DIGEST_PARAM.matcher(rest);
        while (m.find()) {
            String key = m.group(1);
            String q = m.group(3);
            String u = m.group(4);
            String val = q != null ? q : (u != null ? u.trim() : "");
            map.put(key, val);
        }
        return map;
    }

    static String buildAuthorizationHeader(
            String method,
            URI uri,
            String username,
            String password,
            Map<String, String> challenge) {

        String realm = challenge.get("realm");
        String nonce = challenge.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }

        String qop = challenge.get("qop");
        String opaque = challenge.get("opaque");
        String algorithm = challenge.getOrDefault("algorithm", "MD5").trim();
        String algorithmUpper = algorithm.toUpperCase(Locale.ROOT);
        boolean md5 = "MD5".equals(algorithmUpper);
        boolean md5Sess = "MD5-SESS".equals(algorithmUpper);
        if (!md5 && !md5Sess) {
            return null;
        }

        String digestUri = digestUri(uri);
        String ha2 = md5Hex(method.toUpperCase(Locale.ROOT) + ":" + digestUri);

        String responseDigest;
        String stale = challenge.get("stale");

        boolean qopAuth = false;
        if (qop != null) {
            for (String part : qop.split(",")) {
                if ("auth".equalsIgnoreCase(part.trim().replace("\"", ""))) {
                    qopAuth = true;
                    break;
                }
            }
        }

        if (qopAuth) {
            String stateKey = digestStateKey(uri, username, realm);
            boolean isStale = stale != null && "true".equalsIgnoreCase(stale.trim().replace("\"", ""));
            DigestState state = DIGEST_STATE.compute(stateKey, (k, existing) -> {
                if (isStale || existing == null || !nonce.equals(existing.nonce)) {
                    byte[] cnonceBytes = new byte[8];
                    SECURE_RANDOM.nextBytes(cnonceBytes);
                    return new DigestState(nonce, toHex(cnonceBytes), 1);
                }
                return existing;
            });
            int ncInt = state.nc.getAndIncrement();
            String nc = String.format(Locale.ROOT, "%08x", ncInt);
            String cnonce = state.cnonce;

            String ha1 = md5Hex(username + ":" + realm + ":" + password);
            if (md5Sess) {
                // RFC 2617: HA1 = MD5( MD5(username:realm:password) : nonce : cnonce )
                ha1 = md5Hex(ha1 + ":" + nonce + ":" + cnonce);
            }

            String qopToken = "auth";
            responseDigest = md5Hex(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qopToken + ":" + ha2);
            StringBuilder sb = new StringBuilder(256);
            sb.append("Digest username=\"").append(escapeQuoted(username))
                    .append("\", realm=\"").append(escapeQuoted(realm))
                    .append("\", nonce=\"").append(escapeQuoted(nonce))
                    .append("\", uri=\"").append(escapeQuoted(digestUri))
                    .append("\", response=\"").append(responseDigest)
                    .append("\", qop=").append(qopToken)
                    .append(", nc=").append(nc)
                    .append(", cnonce=\"").append(cnonce).append("\"");
            if (opaque != null) {
                sb.append(", opaque=\"").append(escapeQuoted(opaque)).append("\"");
            }
            sb.append(", algorithm=").append(algorithmUpper);
            return sb.toString();
        }

        String ha1 = md5Hex(username + ":" + realm + ":" + password);
        responseDigest = md5Hex(ha1 + ":" + nonce + ":" + ha2);
        StringBuilder sb = new StringBuilder(256);
        sb.append("Digest username=\"").append(escapeQuoted(username))
                .append("\", realm=\"").append(escapeQuoted(realm))
                .append("\", nonce=\"").append(escapeQuoted(nonce))
                .append("\", uri=\"").append(escapeQuoted(digestUri))
                .append("\", response=\"").append(responseDigest).append("\"");
        if (opaque != null) {
            sb.append(", opaque=\"").append(escapeQuoted(opaque)).append("\"");
        }
        sb.append(", algorithm=").append(algorithmUpper);
        return sb.toString();
    }

    private static String digestStateKey(URI uri, String username, String realm) {
        String host = uri.getHost() != null ? uri.getHost() : "";
        int port = uri.getPort();
        String hostPort = port > 0 ? host + ":" + port : host;
        return hostPort + "|" + realm + "|" + username;
    }

    private static String escapeQuoted(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String digestUri(URI uri) {
        String path = uri.getRawPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        String query = uri.getRawQuery();
        return query == null ? path : path + "?" + query;
    }

    private static String md5Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return toHex(md.digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }
}
