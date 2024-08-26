package crawling_test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JumpitCrawling {

    public static void main(String[] args) throws Exception {
        // EdgeDriver 위치 설정
        System.setProperty("webdriver.edge.driver", "D:\\tools\\edgedriver_win64\\msedgedriver.exe");

        // 직무 ID 목록 가져오기
        String[] ids = JumpitJobIdCrawler.getJobIds();
        List<Map<String, Object>> jobDataList = select(ids);

        // JSON으로 변환 (Gson 사용)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResult = gson.toJson(jobDataList);

        // 처음 3개의 JSON 객체 출력
        for (int i = 0; i < Math.min(jobDataList.size(), 3); i++) {
            System.out.println(gson.toJson(jobDataList.get(i)));
        }

        // 파일 이름에 타임스탬프 추가
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "C:\\Users\\KDP\\Desktop\\json\\job_data_" + timestamp + ".json";

        // JSON 파일로 저장
        saveJsonToFile(jsonResult, fileName);
        System.out.println("JSON 파일이 저장되었습니다: " + fileName);
    }

    public static List<Map<String, Object>> select(String[] jobIds) throws Exception {
        List<Map<String, Object>> jobDataList = new ArrayList<>();
        WebDriver driver = new EdgeDriver();

        try {
            for (String jobId : jobIds) {
                String url = "https://www.jumpit.co.kr/position/" + jobId;
                driver.get(url);

                // 페이지 로딩 대기
                Thread.sleep(1000);

                // 페이지 소스를 가져와 Jsoup으로 파싱
                Document doc = Jsoup.parse(driver.getPageSource());

                // 공고명 크롤링
                Element mainHeading = doc.selectFirst("div.sc-f491c6ef-0");
                String mainText = mainHeading != null ? mainHeading.selectFirst("h1").text().trim() : "Not found";

                // '병역특례'라는 단어가 공고명에 포함되어 있으면 이 공고를 제외
                if (mainText.contains("병역특례")) {
                    continue;
                }

                // 회사명 크롤링
                Element companyLink = doc.selectFirst("a.name");
                String companyName = companyLink != null ? companyLink.text().trim() : "Not found";

                // 기술스택 크롤링
                List<String> techStackList = new ArrayList<>();
                Element techStackDl = doc.selectFirst("dl.sc-e76d2562-0");
                if (techStackDl != null) {
                    Element techStackDd = techStackDl.selectFirst("dd");
                    if (techStackDd != null) {
                        for (Element div : techStackDd.select("div.sc-d9de2de1-0")) {
                            techStackList.add(div.text().trim());
                        }
                    }
                }

                // 주요업무 크롤링 (배열로 변환)
                Element workInfoDl = doc.select("dl.sc-e76d2562-0").get(1);
                List<String> workInfo = workInfoDl != null
                        ? List.of(workInfoDl.select("dd pre").text().split("\n"))
                        : List.of("Not found");

                // 자격요건 크롤링 (배열로 변환)
                Element qualificationsDl = doc.select("dl.sc-e76d2562-0").get(2);
                List<String> qualifications = qualificationsDl != null
                        ? List.of(qualificationsDl.select("dd pre").text().split("\n"))
                        : List.of("Not found");

                // 우대사항 크롤링 (배열로 변환)
                Element preferredDl = doc.select("dl.sc-e76d2562-0").get(3);
                List<String> preferred = preferredDl != null
                        ? List.of(preferredDl.select("dd pre").text().split("\n"))
                        : List.of("Not found");

                // JSON 형식으로 데이터 구성
                Map<String, Object> jobData = new LinkedHashMap<>();
                jobData.put("title", mainText);
                jobData.put("job_id", jobId);
                jobData.put("company_name", companyName);
                jobData.put("tech_stack", techStackList);
                jobData.put("work_info", workInfo); // 주요업무 배열 추가
                jobData.put("qualifications", qualifications);
                jobData.put("preferred", preferred);

                // 결과를 리스트에 추가
                jobDataList.add(jobData);
            }
        } finally {
            driver.quit(); // WebDriver 종료
        }

        return jobDataList;
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