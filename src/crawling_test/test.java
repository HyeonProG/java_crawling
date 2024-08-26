package crawling_test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class test {

    // WebDriver를 클래스 레벨의 static 변수로 선언
    private static WebDriver driver;

    // static 블록: 클래스가 로드될 때 WebDriver를 초기화
    static {
        // EdgeDriver 경로 설정
        System.setProperty("webdriver.edge.driver", "D:\\tools\\edgedriver_win64\\msedgedriver.exe");

        // WebDriver 객체 생성
        driver = new EdgeDriver();
    }

    // getJobIds 메서드: static 메서드
    public static List<String> getJobIds() {
        List<String> jobIdList = new ArrayList<>();
        driver.get("https://groupby.kr/positions");

        // JavaScriptExecutor 생성
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 첫 번째 caret-down 이미지 클릭
        WebElement caretImage1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='caret-down']")));
        caretImage1.click();

        // "개발" 항목 클릭
        WebElement developmentElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class, 'sc-231dc133-0') and contains(@class, 'sc-7497f8df-0') and contains(@class, 'eWIWaB')]")));
        developmentElement.click();

        caretImage1.click();

        // 두 번째 caret-down 이미지 클릭
        WebElement caretImage2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='caret-down']")));
        caretImage2.click();
        
        // "신입" 항목 클릭
        WebElement juniorElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class, 'sc-231dc133-0') and contains(@class, 'sc-7497f8df-0') and contains(@class, 'fQNUGZ')]")));
        developmentElement.click();

        caretImage2.click();
        
        
        // 페이지 스크롤하여 모든 공고 로드
        while (true) {
            List<WebElement> jobElements = driver.findElements(By.xpath("//span[contains(@class, 'sc-b4d34b2a-0') and contains(@class, 'clickable')]"));

            // 스크롤을 내려서 더 많은 공고를 로드합니다.
            js.executeScript("window.scrollBy(0, document.body.scrollHeight);");

            // 잠시 대기하여 공고가 로드될 시간을 줍니다.
            try {
                Thread.sleep(2000); // 페이지가 로드될 시간을 조절합니다.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 로드된 공고가 더 이상 없으면 루프 종료
            if (jobElements.isEmpty()) {
                break;
            }
        }

        // 모든 공고를 클릭하고 처리
        List<WebElement> jobElements = driver.findElements(By.xpath("//span[contains(@class, 'sc-b4d34b2a-0') and contains(@class, 'clickable')]"));
        for (WebElement titleElement : jobElements) {
            try {
                // 공고명을 클릭
                js.executeScript("arguments[0].scrollIntoView(true);", titleElement);
                titleElement.click(); // JavaScript로 클릭은 제거하고 직접 클릭

                // 잠시 기다려서 모달이 로드될 시간을 줍니다.
                Thread.sleep(1000);

                // 공고 ID 추출 (예시: 공고 ID가 URL에 포함되어 있다고 가정)
                // 실제로는 공고 페이지에서 ID를 어떻게 추출할지에 따라 아래 코드 수정 필요
                String jobId = driver.getCurrentUrl(); // URL에서 ID를 추출하는 방법을 구현
                jobIdList.add(jobId);

                // 비동기로 열린 페이지 닫기
                WebElement closeElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                        "//div[@class='sc-231dc133-0 sc-65d87496-2 gSwPOu likGQv']//span[contains(text(), '닫기')]")));
                js.executeScript("arguments[0].scrollIntoView(true);", closeElement);
                closeElement.click(); // JavaScript로 클릭은 제거하고 직접 클릭

                // 잠시 기다려서 페이지가 닫힐 시간을 줍니다.
                Thread.sleep(1000);
                
            } catch (Exception e) {
                System.out.println("공고를 처리하는 중 오류 발생: " + e.getMessage());
            }
        }

        return jobIdList;
    }

    public static void main(String[] args) {
        // 메인 메서드에서 getJobIds 호출 (테스트용)
        List<String> jobIds = getJobIds();

        // 결과 출력 (테스트용)
        for (String jobId : jobIds) {
            System.out.println(jobId);
        }

        // 브라우저 닫기 (테스트 시 주석 처리, 실제 사용 시 주석 해제)
//        driver.quit();
    }
}
