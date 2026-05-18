package com.nastya;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GszSource implements JobSource {

    // Обход SSL сертификатов
    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {}
    }

    @Override
    public List<Vacancy> getVacancies(String query) {
        List<Vacancy> result = new ArrayList<>();
        try {
            // Твой найденный URL для поиска
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://gsz.gov.by/registration/vacancy-search/?profession=" + encodedQuery;

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/123.0.0.0")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            // Ищем блоки вакансий. На этой странице они обычно в div-ах или таблицах
            // Попробуем достать все ссылки, которые ведут на описание вакансии
            Elements elements = doc.select("a[href*='/vacancy/']");

            for (Element link : elements) {
                String title = link.text();
                String urlVac = link.attr("abs:href");

                // Пытаемся найти компанию и зарплату в родительском блоке ссылки
                Element parent = link.parent().parent();
                String company = parent.select(".vacancy-company, .company").text();
                String salary = parent.select(".vacancy-salary, .salary").text();

                if (title.length() > 5) {
                    result.add(new Vacancy(title,
                            company.isEmpty() ? "Гос. организация" : company,
                            salary.isEmpty() ? "По гос. сетке" : salary,
                            urlVac,
                            "Гос. служба занятости"));
                }
                if (result.size() >= 30) break;
            }
        } catch (Exception e) {
            System.err.println("GSZ (Служба занятости) ошибка: " + e.getMessage());
        }
        return result;
    }
}