package crawling_test;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class GroupByIdCrawler {

    // WebDriver를 클래스 레벨의 static 변수로 선언
    private static WebDriver driver;

    // static 블록: 클래스가 로드될 때 WebDriver를 초기화
    static {
        // EdgeDriver 위치 설정
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\HighTech\\Desktop\\sts\\chromedriver-win64\\chromedriver.exe");
        driver = new ChromeDriver();
    }

    // getJobIds 메서드: static 메서드
    public static String[] getJobIds() {
        List<String> jobIdList = new ArrayList<>();
        driver.get("https://groupby.kr/positions");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        int loadedJobs = 0;

        while (loadedJobs < 150) {
            // 현재 페이지에서 job cards를 찾습니다.
            List<WebElement> jobCards = driver.findElements(By.cssSelector("div.sc-d609d44f-0.grDLmW > a"));

            for (WebElement jobCard : jobCards) {
                // href 속성에서 jobId 추출
                String href = jobCard.getAttribute("href");

                // href에서 직무 ID 추출
                if (href != null && href.contains("/position/")) {
                    String jobId = href.substring(href.lastIndexOf("/position/") + 10);
                    jobIdList.add(jobId);
                    loadedJobs++;
                    if (loadedJobs >= 150) {
                        break;
                    }
                }
            }

            // 스크롤을 페이지 하단으로 이동
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // 새로운 데이터가 로드될 시간을 줍니다.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 드라이버 종료
        driver.quit();

        // 리스트를 배열로 변환하여 반환
        return jobIdList.toArray(new String[0]);
    }

    public static void main(String[] args) {
        // 직무 ID 목록 가져오기
        String[] jobIds = getJobIds();

        // 가져온 직무 ID 출력
        for (String jobId : jobIds) {
            System.out.println("Job ID: " + jobId);
            System.out.println();
        }
        System.out.println("Total Job IDs: " + jobIds.length);
    }
}
