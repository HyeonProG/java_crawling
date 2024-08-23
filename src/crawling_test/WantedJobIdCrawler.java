package crawling_test;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

public class WantedJobIdCrawler {

    // WebDriver를 클래스 레벨의 static 변수로 선언
    private static WebDriver driver;

    // static 블록: 클래스가 로드될 때 WebDriver를 초기화
    static {
        // EdgeDriver 위치 설정
        System.setProperty("webdriver.edge.driver", "D:\\tools\\edgedriver_win64\\msedgedriver.exe");
        driver = new EdgeDriver();
    }

    // getJobIds 메서드: static 메서드
    public static String[] getJobIds() {
        List<String> jobIdList = new ArrayList<>();
        driver.get("https://www.wanted.co.kr/wdlist/518?country=kr&job_sort=job.recommend_order&years=0&locations=all");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        int loadedJobs = 0;

        while (loadedJobs < 150) {
            // 현재 페이지에서 job cards를 찾습니다.
            List<WebElement> jobCards = driver.findElements(By.cssSelector("div.JobCard_JobCard__thumb__WU1ax"));

            for (WebElement jobCard : jobCards) {
                // data-position-id와 data-position-name을 가져옵니다.
                String positionId = jobCard.findElement(By.cssSelector("button.bookmarkBtn")).getAttribute("data-position-id");
                String positionName = jobCard.findElement(By.cssSelector("button.bookmarkBtn")).getAttribute("data-position-name");

                // "장애인" 또는 "보충역"이 포함된 경우 제외
                if (!positionName.contains("장애인") && !positionName.contains("보충역")) {
                    jobIdList.add(positionId);
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
}
