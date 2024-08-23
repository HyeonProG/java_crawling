package crawling_test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WantedCrawling2 {

    public static void main(String[] args) throws Exception {
        // EdgeDriver 위치 설정
        System.setProperty("webdriver.edge.driver", "D:\\tools\\edgedriver_win64\\msedgedriver.exe");

        String[] ids = { "22685" };
        List<Map<String, Object>> jobDataList = select(ids);

        // JSON으로 변환 (Gson 사용)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResult = gson.toJson(jobDataList);

        System.out.println(jsonResult);
//        // JSON 파일로 저장
//        saveJsonToFile(jsonResult, "C:\\Users\\KDP\\Desktop\\json\\job_data.json");
//
//        System.out.println("JSON 파일이 저장되었습니다.");
    }

    public static List<Map<String, Object>> select(String[] jobIds) throws Exception {
        List<Map<String, Object>> jobDataList = new ArrayList<>();
        WebDriver driver = new EdgeDriver();

        try {
            for (String jobId : jobIds) {
                String url = "https://www.jumpit.co.kr/position" + jobId;
                driver.get(url);

                // "상세 정보 더 보기" 버튼 클릭
                try {
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//                    WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[./span[text()='상세 정보 더 보기']]")));
//                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);

                    // 페이지가 로드될 때까지 기다림
//                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[contains(text(), '우대사항')]")));

                } catch (Exception e) {
                    System.out.println("상세 정보 더 보기 버튼을 찾을 수 없거나 클릭할 수 없음: " + e.getMessage());
                    continue; // 다음 jobId로 이동
                }

                // 페이지 소스를 가져와 Jsoup으로 파싱
                Document doc = Jsoup.parse(driver.getPageSource());

                // 공고명 크롤링
                Element mainHeading = doc.selectFirst("h1[class*=JobHeader_JobHeader__PositionName__]");
                String mainText = mainHeading != null ? mainHeading.text().trim() : "Not found";

                // 회사명 크롤링
                Element companyLink = doc.selectFirst("a[class*=JobHeader_JobHeader__Tools__Company__Link__]");
                String companyName = companyLink != null ? companyLink.text().trim() : "Not found";
                String companyUrl = companyLink != null ? companyLink.attr("href") : "Not found";

                // 위치 정보 크롤링
                Element locationSpan = doc.selectFirst("span[class*=JobHeader_JobHeader__Tools__Company__Info__]");
                String locationText = "Not found"; // 기본값 설정
                if (locationSpan != null) {
                    locationText = locationSpan.text().trim();
                }

                // 주요업무 크롤링
                Element dutiesHeading = doc.select("h3:contains(주요업무)").first();
                String dutiesHtml = dutiesHeading != null ? dutiesHeading.nextElementSibling().html().trim()
                        : "Not found";

                // 자격요건 크롤링
                Element qualificationsHeading = doc.select("h3:matchesOwn(자격요건)").first();
                String qualificationsHtml = qualificationsHeading != null
                        ? qualificationsHeading.nextElementSibling().html().trim()
                        : "Not found";

                // 우대사항 크롤링
                Element preferredQualificationsHeading = doc.select("h3:matchesOwn(우대사항)").first();
                String preferredQualificationsHtml = preferredQualificationsHeading != null
                        ? preferredQualificationsHeading.nextElementSibling().html().trim()
                        : "Not found";

                // 기술 태그 크롤링
                List<String> skillTagsList = new ArrayList<>();
                Element skillTagsListElement = doc.selectFirst("ul.JobSkillTags_JobSkillTags__list__01GRk");
                if (skillTagsListElement != null) {
                    for (Element li : skillTagsListElement.select("li.SkillTagItem_SkillTagItem__K3B3t span")) {
                        skillTagsList.add(li.text().trim());
                    }
                } else {
                    skillTagsList.add("Not found");
                }

                // `end_date` 크롤링
                String endDate = "Not found"; // 기본값 설정
                Element endDateElement = doc.selectFirst("h2:contains(마감일)");
                if (endDateElement != null) {
                    Element endDateSibling = endDateElement.nextElementSibling();
                    if (endDateSibling != null) {
                        endDate = endDateSibling.text().trim();
                    }
                }

                // HTML 태그 제거 및 문장별로 나누기
                List<String> dutiesList = splitIntoLines(dutiesHtml);
                List<String> qualificationsList = splitIntoLines(qualificationsHtml);
                List<String> preferredQualificationsList = splitIntoLines(preferredQualificationsHtml);

                // JSON 형식으로 데이터 구성 (순서에 맞게)
                Map<String, Object> jobData = new LinkedHashMap<>();
                jobData.put("title", mainText);
                jobData.put("job_id", jobId);
                jobData.put("company_name", companyName);
                jobData.put("location", locationText);
                jobData.put("company_url", companyUrl);
                jobData.put("qualifications", qualificationsList);
                jobData.put("duties", dutiesList);
                jobData.put("preferred_qualifications", preferredQualificationsList); // 우대사항 추가
                jobData.put("skills", skillTagsList); // 기술 태그 추가
                jobData.put("end_date", endDate); // end date 추가

                // 결과를 리스트에 추가
                jobDataList.add(jobData);
            }
        } finally {
            driver.quit(); // WebDriver 종료
        }

        return jobDataList;
    }

    // HTML 태그를 제거하고 각 항목을 리스트로 반환하는 메소드
    private static List<String> splitIntoLines(String html) {
        List<String> lines = new ArrayList<>();
        if (html != null && !html.equals("Not found")) {
            Document doc = Jsoup.parse(html);
            String[] split = doc.text().split("•|\\*|-|—"); // '•', '*', '-', '—'를 기준으로 문장 분리
            for (String line : split) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
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
