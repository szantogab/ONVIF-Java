package be.teletask.onvif.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
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

    private HttpDigest() {
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

        String authHeader = findDigestChallenge(response.headers());
        if (authHeader == null) {
            return response;
        }

        Map<String, String> params = parseDigestChallenge(authHeader);
        if (params.isEmpty()) {
            return response;
        }

        String authorization = buildAuthorizationHeader(method, uri, username, password == null ? "" : password, params);
        if (authorization == null) {
            return response;
        }

        HttpRequest retry = buildRequest(method, uri, headers, body, timeout, authorization);
        return client.send(retry, HttpResponse.BodyHandlers.ofString());
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

        String authHeader = findDigestChallenge(response.headers());
        if (authHeader == null) {
            return response;
        }

        Map<String, String> params = parseDigestChallenge(authHeader);
        if (params.isEmpty()) {
            return response;
        }

        String authorization = buildAuthorizationHeader(method, uri, username, password == null ? "" : password, params);
        if (authorization == null) {
            return response;
        }

        HttpRequest retry = buildRequest(method, uri, headers, body, timeout, authorization);
        return client.send(retry, HttpResponse.BodyHandlers.ofByteArray());
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

    private static String findDigestChallenge(java.net.http.HttpHeaders headers) {
        List<String> values = headers.allValues("WWW-Authenticate");
        for (String v : values) {
            if (v != null && v.trim().regionMatches(true, 0, "Digest", 0, 6)) {
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
        String algorithm = challenge.getOrDefault("algorithm", "MD5");

        if (!algorithm.toUpperCase(Locale.ROOT).startsWith("MD5")) {
            return null;
        }

        String digestUri = digestUri(uri);
        String ha1 = md5Hex(username + ":" + realm + ":" + password);
        String ha2 = md5Hex(method.toUpperCase(Locale.ROOT) + ":" + digestUri);

        String responseDigest;
        String nc = "00000001";
        byte[] cnonceBytes = new byte[8];
        SECURE_RANDOM.nextBytes(cnonceBytes);
        String cnonce = toHex(cnonceBytes);

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
            sb.append(", algorithm=MD5");
            return sb.toString();
        }

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
        sb.append(", algorithm=MD5");
        return sb.toString();
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
