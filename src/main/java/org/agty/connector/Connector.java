package org.agty.connector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Connector {
    /** Версия*/
    private final String version = "1.2.0";

    /** Ссылка на ресурс*/
    private String sourceResourceURL = "";

    /** Метод передачи данных*/
    private String requestMethod = "GET";

    /** Кодировка по умолчанию*/
    private String DEFAULT_ENCODING = "UTF-8";

    /** Безопасное соединение*/
    private boolean ssl = false;

    /** Тайминги соединения*/
    private final int CONNECTION_TIMEOUT = 30000;
    private final int READ_TIMEOUT = 30000;

    /** Параметры запроса (Заголовки)*/
    private final Map<String, String> requestProperty = new HashMap<>();

    /** Переменная соединения*/
    private HttpURLConnection connection;

    /** Содержание POST-запроса*/
    private String post = "";
    private final Map<String, String> postArray = new HashMap<>();

    /** Проверка сертификатов включена по умолчанию*/
    private boolean checkCert = true;

    /** POST запрос в формате JSON*/
    private boolean isPostJson = false;

    /** Текущая ошибка*/
    private String error = "";

    /** Текущий код ошибки*/
    private int errorCode = 0;

    /**
     * Настройки прокси
     */
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPass;
    private String proxyType;

    /**
     * Конструктор.
     *
     * @param sourceResourceURL ссылка на ресурс.
     */
    public Connector(String sourceResourceURL) {
        this.sourceResourceURL = sourceResourceURL;
    }

    /**
     * Конструктор.
     *
     * @param sourceResourceURL ссылка на ресурс.
     * @param requestMethod метод передачи данных.
     */
    public Connector(String sourceResourceURL, String requestMethod) {
        this.sourceResourceURL = sourceResourceURL;
        setRequestMethod(requestMethod);
    }

    /**
     * Конструктор.
     *
     * @param sourceResourceURL ссылка на ресурс.
     * @param requestMethod метод передачи данных.
     * @param ssl если ссылка является HTTPS = true.
     */
    public Connector(String sourceResourceURL, String requestMethod, boolean ssl) {
        this.sourceResourceURL = sourceResourceURL;
        setRequestMethod(requestMethod);
        this.ssl = ssl;
    }

    /**
     * Конструктор.
     *
     * @param sourceResourceURL ссылка на ресурс.
     * @param ssl если ссылка является HTTPS = true.
     */
    public Connector(String sourceResourceURL, boolean ssl) {
        this.sourceResourceURL = sourceResourceURL;
        this.ssl = ssl;
    }

    /**
     * Версия библиотеки
     * @return String версия
     */
    public String getVersion() {
        return version;
    }

    /**
     * Устанавливает ошибку.
     *
     * @param error ошибка.
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Возвращает ошибку.
     * @return ошибка.
     */
    public String getError() {
        return this.error;
    }

    /**
     * Устанавливает код ошибки.
     *
     * @param errorCode код ошибки.
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Возвращает код ошибки.
     * @return код ошибки.
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Проверяет наличие ошибки
     * @return ошибка.
     */
    public boolean isError() {
        return !this.error.isEmpty();
    }

    /**
     * Текущая кодировка.
     * @return string текущая кодировка.
     */
    public String getEncoding() {
        return DEFAULT_ENCODING;
    }

    /**
     * Назначить кодировку по умолчанию.
     * @param encoding кодировка по умолчанию.
     */
    public void setEncoding(String encoding) {
        this.DEFAULT_ENCODING = encoding;
    }

    /**
     * Текущая ссылка.
     *
     * @return URL ссылка на ресурс.
     */
    public String getURL() {
        return sourceResourceURL;
    }

    /**
     * Текущий метод передачи данных.
     *
     * @return method Метод.
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * Назначить метод передачи данных.
     *
     * @param requestMethod метод передачи данных.
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Проверка наличия ссылки на ресурс.
     *
     * @return boolean true - если ссылка присутствует.
     */
    public boolean isURL() {
        return !this.sourceResourceURL.isEmpty();
    }

    /**
     * Проверка безопасного соединения.
     *
     * @return boolean true - если соединение должно быть безопасным.
     */
    public boolean isSSL() {
        return this.ssl;
    }

    /**
     * Назначить параметр безопасного соединения.
     *
     * @param ssl true - если соединение должно быть безопасным.
     */
    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * Добавить параметр соединения.
     *
     * @param property наименования параметра.
     * @param value значение параметра.
     */
    public Connector setRequestProperty(String property, String value) {
        this.requestProperty.put(property, value);
        return this;
    }

    /**
     * Получить параметр соединения.
     *
     * @param property наименования параметра.
     */
    public String getRequestProperty(String property) {
        return this.requestProperty.get(property);
    }

    /**
     * Удалить параметр соединения.
     *
     * @param property наименования параметра.
     */
    public void removeRequestProperty(String property) {
        this.requestProperty.remove(property);
    }

    /**
     * Отключает\включает проверку сертификата.
     *
     * @param checkCert если = false, сертификат не будет проверен.
     */
    public void setSSLCheck(boolean checkCert) {
        this.checkCert = checkCert;
    }

    /**
     * Добавляет параметры соединения в соединение.
     */
    private void insertRequestProperty() {
        for(Map.Entry<String, String> entry: this.requestProperty.entrySet()) {
            this.connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Параметр POST в запрос.
     *
     * @param post запрос.
     */
    public void setPost(String post) {
        this.post = post;
    }

    /**
     * Получить строку POST.
     */
    public String getPost() {
        return this.post;
    }

    /**
     * Получить массив POST.
     */
    public Map<String, String> getPostArray() {
        return this.postArray;
    }

    /**
     * Использовать или нет JSON при формировании POST запроса.
     *
     * @param isPostJson true, если используется JSON при формировании запроса.
     */
    public void setPostJson(boolean isPostJson) {
        this.isPostJson = isPostJson;
    }

    /**
     * Проверяет, что POST запрос будет в формате JSON.
     *
     * @return true, если явно указано, что необходимо использовать JSON
     */
    public boolean isPostJson() {
        return this.isPostJson;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String host) {
        this.proxyHost = host;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int port) {
        this.proxyPort = port;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String user) {
        this.proxyUser = user;
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public void setProxyPass(String pass) {
        this.proxyPass = pass;
    }

    public Proxy.Type getProxyType() {
        if (proxyType == null) return null;
        return switch(proxyType) {
            case "DIRECT" -> Proxy.Type.DIRECT;
            case "HTTP" -> Proxy.Type.HTTP;
            case "SOCKS" -> Proxy.Type.SOCKS;
            default -> null;
        };
    }

    public void setProxyType(String type) {
        this.proxyType = type;
    }

    /**
     * Создать строку POST из массива.
     *
     * @return string строка POST-запроса.
     */
    public String generatePostFromArray() {
        ConnectorPostValue connectorPostValue = new ConnectorPostValue(this.postArray, this.getEncoding());

        if (this.isPostJson()) {
            return connectorPostValue.getPostJson();
        }

        return connectorPostValue.getPostForm();
    }

    /**
     * Параметр POST в запрос в качестве массива.
     *
     * @param key ключ.
     * @param value значение.
     */
    public Connector setPost(String key, String value) {
        this.postArray.put(key, value);
        return this;
    }

    /**
     * Проверяется наличие POST запроса.
     * @return true если POST существует.
     */
    public boolean isPost() {
        return !this.post.isEmpty() || !this.postArray.isEmpty();
    }

    /**
     * Отключает проверку сертификатов на удаленном сервере
     * -Dcom.sun.net.ssl.checkRevocation=false
     * @author <a href="https://stackoverflow.com/questions/54516557/disable-ssl-certificate-validation-in-java">(stackoverflow) animaonline</a>
     */
    private void noCheckCert() {
        try {
            SSLContext  context = SSLContext.getInstance("TLSv1.2");
            TrustManager[] trustManager = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certificate, String str) {}
                        public void checkServerTrusted(X509Certificate[] certificate, String str) {}
                    }
            };
            context.init(null, trustManager, new SecureRandom());
            ((HttpsURLConnection) this.connection).setSSLSocketFactory(context.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получить текущие настройки прокси
     * @return Proxy объект
     */
    private Proxy getProxy() {
        if (getProxyType() == null || getProxyHost() == null || getProxyPort() < 1) return null;

        return new Proxy(
                getProxyType(),
                new InetSocketAddress(getProxyHost(), getProxyPort())
        );
    }

    private void setProxyAuth() {
        if (getProxyUser() == null || getProxyPass() == null) return;

        Authenticator authenticator = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication(
                        getProxyUser(),
                        getProxyPass().toCharArray()
                ));
            }
        };

        this.connection.setAuthenticator(authenticator);
    }

    /**
     * Создает соединение с удаленным сервером
     */
    public Connector createConnection() {
        if (this.isURL()) {
            try {
                URL url = new URL(this.getURL());
                Proxy proxy = getProxy();

                if (this.isSSL() && getURL().startsWith("https")) {
                    this.connection = proxy == null ?
                            (HttpsURLConnection) url.openConnection()
                            : (HttpsURLConnection) url.openConnection(proxy);

                    if (this.connection != null && !this.checkCert) {
                        this.noCheckCert();
                    }
                } else {
                    this.connection = proxy == null ?
                            (HttpURLConnection) url.openConnection()
                            : (HttpURLConnection) url.openConnection(proxy);
                }

                if (this.connection != null) {
                    if (proxy != null) {
                        setProxyAuth();
                    }

                    this.connection.setRequestMethod(getRequestMethod());
                    this.connection.setConnectTimeout(CONNECTION_TIMEOUT);
                    this.connection.setReadTimeout(READ_TIMEOUT);
                    insertRequestProperty();

                    if (this.isPost()) {
                        if (!this.postArray.isEmpty()) {
                            this.setPost( generatePostFromArray() );
                        }

                        this.connection.setDoOutput(true);
                        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        out.write(getPost().getBytes(getEncoding()));
                        out.flush();
                        out.close();
                    }

                    if (this.connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                        setError(this.connection.getResponseCode() + " " + this.connection.getResponseMessage());
                        setErrorCode(this.connection.getResponseCode());
                    }
                }

            } catch (IOException e) {
                setError(e.getMessage());
                setErrorCode(-1);
            }
        }

        return this;
    }

    public void close() {
        connection.disconnect();
    }

    /**
     * Получить содержимое
     */
    public String getContent() {

        try {
            InputStream inputStream;
            if (this.connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = this.connection.getInputStream();
            } else {
                inputStream = this.connection.getErrorStream();
            }

            if (connection.getContentEncoding() != null && connection.getContentEncoding().equals("gzip")) {
                return getContentGzip(inputStream);
            }

            return getContentPlain(inputStream);

        } catch (final Exception ex) {
            setError(ex.getMessage());
            return null;
        }
    }

    /**
     * Если контент не в сжатом (GZIP) состоянии.
     * @param inputStream Connection InputStream.
     * @return ответ сервера.
     */
    private String getContentPlain(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            final StringBuilder content = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
                content.append("\n");
            }

            return content.isEmpty() ? null : content.toString();

        } catch (final Exception ex) {
            setError(ex.getMessage());
            return null;
        }
    }

    /**
     * Если контент в сжатом (GZIP) состоянии.
     * @param inputStream Connection InputStream.
     * @return ответ сервера.
     */
    private String getContentGzip(InputStream inputStream) {
        try {
            GZIPInputStream gzip = new GZIPInputStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];

            int len;
            while((len = gzip.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            return baos.toString();

        } catch (final Exception ex) {
            setError(ex.getMessage());
            return null;
        }
    }

    /**
     * Получить заголовки
     * @return коллекция с заголовками
     */
    public Map<String, List<String>> getHeaders() {
        if (isError()) return null;
        return this.connection.getHeaderFields();
    }

    public int getResponseCode() {
        try {
            return this.connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}