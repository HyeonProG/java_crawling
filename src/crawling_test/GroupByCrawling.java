package crawling_test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GroupByCrawling {

    public static void main(String[] args) throws Exception {
        // ChromeDriver 위치 설정
        System.setProperty("webdriver.edge.driver", "D:\\tools\\edgedriver_win64\\msedgedriver.exe");

        String[] ids = { "1443" };
        List<Map<String, Object>> jobDataList = select(ids);

        // JSON으로 변환 (Gson 사용)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResult = gson.toJson(jobDataList);

        System.out.println(jsonResult);
        // JSON 파일로 저장
        // saveJsonToFile(jsonResult, "C:\\Users\\KDP\\Desktop\\json\\job_data.json");
        //
        // System.out.println("JSON 파일이 저장되었습니다.");
    }

    public static List<Map<String, Object>> select(String[] jobIds) throws Exception {
        List<Map<String, Object>> jobDataList = new ArrayList<>();
        WebDriver driver = new EdgeDriver();

        try {
            for (String jobId : jobIds) {
                String url = "https://groupby.kr/positions/" + jobId;
                driver.get(url);

                // 페이지 소스를 가져와 Jsoup으로 파싱
                Document doc = Jsoup.parse(driver.getPageSource());

                // 공고명 크롤링
                String mainText = extractMainText(doc);
                // 회사명 크롤링
                String companyName = extractCompanyName(doc);
                // 위치 정보 크롤링
                String locationText = extractLocation(doc);
                // 주요 업무 크롤링
                String dutiesHtml = extractDutiesHtml(doc);
                // 자격 요건 크롤링
                String qualificationsHtml = extractQualificationsHtml(doc);
                // 우대사항 크롤링
                String preferredQualificationsHtml = extractPreferredQualifications(doc);
                // 기술 스택 크롤링
                List<String> techStacks = extractTechStacks(doc);
                // `end_date` 크롤링
                String endDate = extractEndDate(doc);

                // JSON 형식으로 데이터 구성 (순서에 맞게)
                Map<String, Object> jobData = new LinkedHashMap<>();
                jobData.put("title", mainText);
                jobData.put("job_id", jobId);
                jobData.put("company_name", companyName);
                jobData.put("location", locationText);
                jobData.put("qualifications", extractQualifications(qualificationsHtml));
                jobData.put("duties", extractDuties(dutiesHtml));
                jobData.put("preferred_qualifications", splitIntoLines(preferredQualificationsHtml)); // 우대사항 추가
                jobData.put("skills", techStacks); // 기술 스택 추가
                jobData.put("end_date", endDate); // end date 추가

                // 결과를 리스트에 추가
                jobDataList.add(jobData);
            }
        } finally {
            driver.quit(); // WebDriver 종료
        }

        return jobDataList;
    }

    // HTML에서 공고명 추출
    private static String extractMainText(Document doc) {
        Element mainHeading = doc.selectFirst("span.sc-b4d34b2a-0.sc-b4d34b2a-5.sc-3b0bbe22-0.ejFnZT.fSnXZc.gemwaf");
        return mainHeading != null ? mainHeading.text().trim() : "Not found";
    }

    // HTML에서 회사명 추출
    private static String extractCompanyName(Document doc) {
        Element companyNameElement = doc.selectFirst("span.sc-b4d34b2a-0.sc-b4d34b2a-9.sc-95190c8e-0.ejFnZT.fLSbGn.bDBwAK");
        return companyNameElement != null ? companyNameElement.text().trim() : "Not found";
    }

    // HTML에서 위치 정보 추출
    private static String extractLocation(Document doc) {
        Element locationSpan = doc.selectFirst("span.sc-b4d34b2a-0.sc-b4d34b2a-7.sc-cc09b216-2.ejFnZT.dTAHIz.kFltgO");
        return locationSpan != null ? locationSpan.text().trim() : "Not found";
    }

    // HTML에서 주요 업무 HTML 추출
    private static String extractDutiesHtml(Document doc) {
        List<Element> editors = doc.select("div.ql-editor");
        return editors.size() > 0 ? editors.get(0).html().trim() : "Not found";
    }

    // HTML에서 자격 요건 HTML 추출
    private static String extractQualificationsHtml(Document doc) {
        List<Element> editors = doc.select("div.ql-editor");
        return editors.size() > 1 ? editors.get(1).html().trim() : "Not found";
    }

    // HTML에서 우대사항 추출
    private static String extractPreferredQualifications(Document doc) {
        List<Element> preferredQualificationsElements = doc.select("div.ql-editor");
        if (preferredQualificationsElements.size() > 2) {
            Element preferredQualificationsElement = preferredQualificationsElements.get(2);
            if (preferredQualificationsElement != null) {
                return preferredQualificationsElement.html().trim();
            }
        }
        return "Not found";
    }

    // HTML에서 기술 스택 추출
    private static List<String> extractTechStacks(Document doc) {
        List<String> techStacks = new ArrayList<>();
        Element techStacksElement = doc.selectFirst("div.sc-231dc133-0.sc-c5d6490f-1.gSwPOu.erGksm");
        if (techStacksElement != null) {
            for (Element span : techStacksElement.select("div.sc-c5d6490f-0.gtALbX span.sc-b4d34b2a-0.ejFnZT")) {
                techStacks.add(span.text().trim());
            }
        } else {
            techStacks.add("Not found");
        }
        return techStacks;
    }

    // HTML에서 end date 추출
    private static String extractEndDate(Document doc) {
        String endDate = "Not found"; // 기본값 설정
        Element endDateElement = doc.selectFirst("h2:contains(마감일)");
        if (endDateElement != null) {
            Element endDateSibling = endDateElement.nextElementSibling();
            if (endDateSibling != null) {
                endDate = endDateSibling.text().trim();
            }
        }
        return endDate;
    }

    // HTML에서 주요 업무 추출
    private static List<String> extractDuties(String html) {
        List<String> duties = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        for (Element element : doc.select("ul > li")) {
            duties.add(element.text().trim());
        }

        if (duties.isEmpty()) {
            duties.add("Not found");
        }

        return duties;
    }

    // HTML에서 자격 요건 추출
    private static List<String> extractQualifications(String html) {
        List<String> qualifications = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        // 자격요건이 <ul> 안에 <li>로 나열
        Element qualificationsListElement = doc.selectFirst("ul");
        if (qualificationsListElement != null) {
            for (Element li : qualificationsListElement.select("li")) {
                qualifications.add(li.text().trim());
            }
        } else {
            qualifications.add("Not found");
        }

        return qualifications;
    }

    // HTML 태그를 제거하고 각 항목을 리스트로 반환하는 메소드
    private static List<String> splitIntoLines(String html) {
        List<String> lines = new ArrayList<>();
        if (html != null && !html.equals("Not found")) {
            Document doc = Jsoup.parse(html);
            for (Element element : doc.select("ul > li")) {
                lines.add(element.text().trim());
            }
        } else {
            lines.add("Not found");
        }
        return lines;
    }

    private static void saveJsonToFile(String jsonString, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(jsonString);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
